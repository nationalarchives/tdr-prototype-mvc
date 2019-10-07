package controllers

import akka.http.scaladsl.model.headers.RawHeader
import graphql.GraphQLClientProvider
import graphql.codegen.GetAllSeries
import javax.inject._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import model.SelectedSeriesData

import scala.concurrent.ExecutionContext

@Singleton
class SeriesDetailsController @Inject()(
                                         client: GraphQLClientProvider,
                                         controllerComponents: ControllerComponents,
                                         configuration: Configuration
                                       )(implicit val ex: ExecutionContext)extends AbstractController(controllerComponents) {

  val selectedSeriesForm = Form(
    mapping(
      "seriesNo" -> number
    )(SelectedSeriesData.apply)(SelectedSeriesData.unapply)
  )

  def submit() = Action { implicit request: Request[AnyContent] =>

    var seriesData = selectedSeriesForm.bindFromRequest.get
    var selectedSeriesId = seriesData.seriesId

    Redirect(routes.CreateConsignmentController.index(selectedSeriesId))
  }

  def index() = Action.async { implicit request: Request[AnyContent] =>

    val graphQlClient = client.graphqlClient

    graphQlClient.query[GetAllSeries.getAllSeries.Data](GetAllSeries.getAllSeries.document).result.map(result => result match {
      case Right(r) => Ok(views.html.seriesDetails(r.data.getAllSeries, selectedSeriesForm))
      case Left(ex) => InternalServerError(ex.errors.toString())
    })
  }
}
