/*
 *
 * This code is basically the GraphQLCursor code from
 * https://github.com/Jarlakxen/drunk except contstructor requires a TdRBackendClient
 * This allows the code to be used with GenericApiGatewayClient
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

import com.github.jarlakxen.drunk.GraphQLOperation
import com.github.jarlakxen.drunk.extensions._
import io.circe._

import scala.concurrent.{ExecutionContext, Future}

class TdrGraphQLCursor[Res, Vars](
                                   client: TdrGraphQLClient,
                                   val result: Future[TdrGraphQLClient.GraphQLResponse[Res]],
                                   val extensions: Future[GraphQLExtensions],
                                   val lastOperation: GraphQLOperation[Res, Vars])(implicit responseDecoder: Decoder[Res], ec: ExecutionContext) {

  def refetch: TdrGraphQLCursor[Res, Vars] =
    refetch(None)

  def fetchMore(variables: Vars): TdrGraphQLCursor[Res, Vars] =
    refetch(Some(variables))

  def fetchMore(newVars: Vars => Vars): TdrGraphQLCursor[Res, Vars] =
    refetch(lastOperation.variables.map(newVars(_)))

  private def refetch(variables: Option[Vars]): TdrGraphQLCursor[Res, Vars] = {
    implicit val variablesEncoder = lastOperation.variablesEncoder
    client.query(lastOperation.doc, variables, lastOperation.name)
  }
}