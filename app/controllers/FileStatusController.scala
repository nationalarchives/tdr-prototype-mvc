package controllers

import akka.http.scaladsl.model.headers.RawHeader
import graphql.GraphQLClientProvider
import graphql.codegen.GetFileStatus.getFileChecksStatus.{Data, Variables, document}
import javax.inject.{Inject, Singleton}
import modules.TDRAttributes
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

@Singleton
class FileStatusController @Inject()(client: GraphQLClientProvider,
                                      cc: ControllerComponents
                                    )(implicit val ex: ExecutionContext) extends AbstractController(cc) {

  def getFileStatus(consignmentId: Int) = Action.async  { implicit request: Request[AnyContent] =>
    val appSyncClient = client.graphqlClient(List())
    appSyncClient.query[Data, Variables](document,Variables(consignmentId)).result.map {
        case Right(r) => Ok(views.html.fileStatus(r.data.getFileChecksStatus))
        case Left(ex) => InternalServerError(ex.errors.toString())
      }
  }
}
