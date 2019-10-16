package controllers

import forms.SelectedSeriesForm
import forms.SelectedSeriesForm.SelectedSeriesData
import graphql.GraphQLClientProvider
import graphql.codegen.GetAllSeries
import javax.inject._
import play.api.Configuration
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SeriesDetailsController @Inject()(
                                         client: GraphQLClientProvider,
                                         controllerComponents: ControllerComponents,
                                         configuration: Configuration
                                       )(implicit val ex: ExecutionContext)extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  def submit() = Action.async { implicit request: Request[AnyContent] =>

    val errorFunction: Form[SelectedSeriesData] => Future[Result] = { formWithErrors: Form[SelectedSeriesData] =>
      retrieveSeriesInformation().map(data =>
        BadRequest(views.html.seriesDetails(data, formWithErrors))
      )
    }
    val successFunction: SelectedSeriesData => Future[Result] = { data: SelectedSeriesData =>
      Future.successful(Redirect(routes.CreateConsignmentController.index(data.seriesId)))
    }

    val formValidationResult: Form[SelectedSeriesData] = SelectedSeriesForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  def index() = Action.async { implicit request: Request[AnyContent] =>
    retrieveSeriesInformation().map(data =>
      Ok(views.html.seriesDetails(data, forms.SelectedSeriesForm.form))
    )
  }

  def retrieveSeriesInformation() = {
    val graphQlClient = client.graphqlClient

    val seriesInformation = graphQlClient.query[GetAllSeries.getAllSeries.Data](GetAllSeries.getAllSeries.document).result

    for {
      info <- seriesInformation
    } yield {
      info.right.get.data.getAllSeries.map(s => s.id.toString -> s.name)
    }
  }
}
