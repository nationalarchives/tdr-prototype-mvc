package controllers

import graphql.GraphQLClientProvider
import graphql.codegen.GetFileStatus.getFileChecksStatus.{Data, GetFileChecksStatus, Variables, document}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

@Singleton
class FileStatusController @Inject()(client: GraphQLClientProvider,
                                      cc: ControllerComponents
                                    )(implicit val ex: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def getFileStatus(consignmentId: Int) = Action.async  { implicit request: Request[AnyContent] =>
    val graphQlClient = client.graphqlClient
    graphQlClient.query[Data, Variables](document,Variables(consignmentId)).result.map {
        case Right(r) => Ok(views.html.fileStatus(r.data.getFileChecksStatus, consignmentId))
        case Left(ex) => InternalServerError(ex.errors.toString())
      }
  }

  implicit val writes = Json.writes[GetFileChecksStatus]

  def getFileStatusApi(consignmentId: Int) = Action.async  { implicit request: Request[AnyContent] =>
    val graphQlClient = client.graphqlClient
    graphQlClient.query[Data, Variables](document,Variables(consignmentId)).result.map {
      case Right(r) => println(Json.toJson(r.data.getFileChecksStatus)); Ok(Json.toJson(r.data.getFileChecksStatus))
      case Left(ex) => InternalServerError(ex.errors.toString())
    }
  }

}
