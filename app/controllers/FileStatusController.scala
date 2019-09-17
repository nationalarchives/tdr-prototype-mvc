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

  def getFileStatus() = Action.async  { implicit request: Request[AnyContent] =>

    val accessToken = request.attrs.get(TDRAttributes.OAuthAccessTokenKey).get.accessToken
    val header = RawHeader("Authorization", accessToken)
    val appSyncClient = client.graphqlClient(List(header))
    appSyncClient.query[Data, Variables](document,Variables(1)).result.map {
        case Right(r) => Ok(views.html.fileStatus(r.data.getFileChecksStatus))
        case Left(ex) => InternalServerError(ex.errors.toString())
      }
  }
}
