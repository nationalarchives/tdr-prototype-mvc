package controllers

import akka.http.scaladsl.model.headers.RawHeader
import graphql.GraphQLClientProvider
import graphql.codegen.GetAllSeries
import javax.inject._
import model.CreateCollectionData
import modules.TDRAttributes
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class SeriesDetailsController @Inject()(
                                         client: GraphQLClientProvider,
                                         controllerComponents: ControllerComponents,
                                         configuration: Configuration
                                       )(implicit val ex: ExecutionContext)extends AbstractController(controllerComponents) {

  def index() = Action.async { implicit request: Request[AnyContent] =>

    val accessToken = request.attrs.get(TDRAttributes.OAuthAccessTokenKey).get.accessToken
    val header = RawHeader("Authorization", accessToken)

    val graphQlClient = client.graphqlClient(List(header))

    graphQlClient.query[GetAllSeries.getAllSeries.Data](GetAllSeries.getAllSeries.document).result.map(result => result match {
      case Right(r) => Ok(views.html.seriesDetails(r.data.getAllSeries))
      case Left(ex) => InternalServerError(ex.errors.toString())
    })
  }
}
