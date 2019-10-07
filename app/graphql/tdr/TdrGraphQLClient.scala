/*
 *
 * This code is basically the GraphQLClient code from
 * https://github.com/Jarlakxen/drunk altered to allow HttpConnection GraohqlServer
 * backend to be AkkaHttpBackend or
 * GenericApiGatewayClient https://github.com/rpgreen/apigateway-generic-java-sdk
 *
 *
 * Copyright 2018 Facundo Viale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package graphql.tdr

import akka.http.scaladsl.model.Uri
import com.github.jarlakxen.drunk.extensions.{GraphQLExtensions, NoExtensions}
import com.github.jarlakxen.drunk.{ast => _, _}
import io.circe._
import io.circe.parser._
import play.api.Configuration
import sangria.ast.Document
import sangria.introspection._
import sangria.marshalling.circe._
import sangria.parser.QueryParser

import scala.concurrent.{ExecutionContext, Future}
import scala.util._

class TdrGraphQLClient(backend: TdrBackendClient)  {

  import TdrGraphQLClient._

  val options = ClientOptions.Default

  def execute[Res, Vars](doc: Document, variables: Option[Vars], name: Option[String])(
    implicit
    variablesEncoder: Encoder[Vars],
    ec: ExecutionContext
  ): Future[(Int, Json)] =
    execute(GraphQLOperation(doc, variables, name))

  def execute[Res, Vars](op: GraphQLOperation[Res, Vars])(
    implicit
    ec: ExecutionContext
  ): Future[(Int, Json)] = {

    val fields =
      List("query" -> op.docToJson) ++
        op.encodeVariables.map("variables" -> _) ++
        op.name.map("operationName" -> Json.fromString(_))

    val body = Json.obj(fields: _*).noSpaces

    for {
      (statusCode, rawBody) <- backend.send(body)
      jsonBody <- Future.fromTry(parse(rawBody).toTry)
    } yield (statusCode, jsonBody)
  }

  def query[Res](doc: String)(implicit dec: Decoder[Res], ec: ExecutionContext): Try[TdrGraphQLCursor[Res, Nothing]] =
    query(doc, None, None)(dec, null, ec)

  def query[Res](
                  doc: String,
                  operationName: String
                )(implicit dec: Decoder[Res], ec: ExecutionContext): Try[TdrGraphQLCursor[Res, Nothing]] =
    query(doc, None, Some(operationName))(dec, null, ec)

  def query[Res, Vars](doc: String, variables: Vars)(
    implicit
    dec: Decoder[Res],
    en: Encoder[Vars],
    ec: ExecutionContext
  ): Try[TdrGraphQLCursor[Res, Vars]] =
    query(doc, Some(variables), None)

  def query[Res, Vars](doc: String, variables: Option[Vars], operationName: Option[String])(
    implicit
    dec: Decoder[Res],
    en: Encoder[Vars],
    ec: ExecutionContext
  ): Try[TdrGraphQLCursor[Res, Vars]] =
    QueryParser.parse(doc).map(query(_, variables, operationName))

  def query[Res](doc: Document)(implicit dec: Decoder[Res], ec: ExecutionContext): TdrGraphQLCursor[Res, Nothing] =
    query(doc, None, None)(dec, null, ec)

  def query[Res](
                  doc: Document,
                  operationName: String
                )(implicit dec: Decoder[Res], ec: ExecutionContext): TdrGraphQLCursor[Res, Nothing] =
    query(doc, None, Some(operationName))(dec, null, ec)

  def query[Res, Vars](doc: Document, variables: Vars)(
    implicit
    dec: Decoder[Res],
    en: Encoder[Vars],
    ec: ExecutionContext
  ): TdrGraphQLCursor[Res, Vars] =
    query(doc, Some(variables), None)

  def query[Res, Vars](doc: Document, variables: Option[Vars], operationName: Option[String])(
    implicit
    dec: Decoder[Res],
    en: Encoder[Vars],
    ec: ExecutionContext
  ): TdrGraphQLCursor[Res, Vars] = {
    var fullDoc = doc
    if (options.addTypename) {
      fullDoc = com.github.jarlakxen.drunk.ast.addTypename(doc)
    }

    val operation: GraphQLOperation[Res, Vars] = GraphQLOperation(doc, variables, operationName)
    val result = execute(operation)
    val data: Future[TdrGraphQLClient.GraphQLResponse[Res]] = result.flatMap {
      case (status, body) => Future.fromTry(extractErrorOrData(body, status))
    }
    val extensions = result.map { case (_, body) => extractExtensions(body) }
    new TdrGraphQLCursor(this, data, extensions, operation)
  }

  def mutate[Res, Vars](doc: Document, variables: Vars)(
    implicit
    dec: Decoder[Res],
    en: Encoder[Vars],
    ec: ExecutionContext
  ): Future[TdrGraphQLClient.GraphQLResponse[Res]] =
    mutate(doc, Some(variables), None)

  def mutate[Res, Vars](doc: Document, variables: Vars, operationName: Option[String])(
    implicit
    dec: Decoder[Res],
    en: Encoder[Vars],
    ec: ExecutionContext
  ): Future[TdrGraphQLClient.GraphQLResponse[Res]] = {

    val result = execute(doc, Some(variables), operationName)
    result.flatMap { case (status, body) => Future.fromTry(extractErrorOrData(body, status)) }
  }

  def schema(implicit ec: ExecutionContext): Future[IntrospectionSchema] =
    execute[Json, Nothing](introspectionQuery, None, None)(null, ec)
      .flatMap {
        case (_, json) => Future.fromTry(IntrospectionParser.parse(json))
      }
}


object TdrGraphQLClient {

  def apply(configuration: Configuration) = new TdrGraphQLClient(new TdrSignRequestClient(configuration))

  def apply(uri: Uri) =  new TdrGraphQLClient(new TdrBackendClientGraphQL(uri))

  // Work arround for Scala 2.11
  implicit class Either212[+A, +B](either: Either[A, B]) {

    def toTry(implicit ev: A <:< Throwable): Try[B] = either match {
      case Right(b) => Success(b)
      case Left(a) => Failure(a)
    }

    def toOption: Option[B] = either match {
      case Right(b) => Some(b)
      case _ => None
    }
  }

  type GraphQLResponse[Res] = Either[GraphQLResponseError, GraphQLResponseData[Res]]

  private[TdrGraphQLClient] def extractErrors(body: Json, statusCode: Int): Option[GraphQLResponseError] = {
    val cursor: HCursor = body.hcursor

    for {
      errorsNode <- cursor.downField("errors").focus
      errors <- errorsNode.asArray
    } yield {
      val msgs = errors.flatMap(_.hcursor.downField("message").as[String].toOption).toList
      GraphQLResponseError(msgs, statusCode)
    }
  }

  private[TdrGraphQLClient] def extractData[Res](jsonBody: Json)(implicit dec: Decoder[Res]): Try[GraphQLResponseData[Res]] =
    jsonBody.hcursor.downField("data").as[Res].toTry.map(GraphQLResponseData(_))

  private[TdrGraphQLClient] def extractErrorOrData[Res](jsonBody: Json, statusCode: Int)(implicit dec: Decoder[Res]): Try[GraphQLResponse[Res]] = {
    val errors: Option[Try[GraphQLResponse[Res]]] =
      extractErrors(jsonBody, statusCode).map(errors => Success(Left(errors)))
    val data: Try[GraphQLResponse[Res]] =
      extractData(jsonBody).map(Right(_))

    errors.getOrElse(data)
  }

  private[TdrGraphQLClient] def extractExtensions(jsonBody: Json): GraphQLExtensions =
    jsonBody
      .hcursor
      .downField("extensions")
      .as[GraphQLExtensions]
      .toOption
      .getOrElse(NoExtensions)

}


