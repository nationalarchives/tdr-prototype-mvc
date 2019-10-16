package controllers

import forms.CreateSeriesForm
import forms.CreateSeriesForm.CreateSeriesData
import graphql.GraphQLClientProvider
import graphql.codegen.CreateSeries
import graphql.codegen.types.CreateSeriesInput
import javax.inject._
import play.api.Configuration
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateSeriesController @Inject()(
                                            client: GraphQLClientProvider,
                                            controllerComponents: ControllerComponents,
                                            configuration: Configuration)(
                                            implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  def index() = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.createSeries(CreateSeriesForm.form))
  }

  def submit() = Action.async { implicit request: Request[AnyContent] =>

    val errorFunction: Form[CreateSeriesData] => Future[Result] = { formWithErrors: Form[CreateSeriesData] =>
      Future.successful(BadRequest(views.html.createSeries(formWithErrors)))
    }
    val successFunction: CreateSeriesData => Future[Result] = { data: CreateSeriesData =>
      val createSeriesInput = CreateSeriesInput(data.seriesName, data.seriesDescription)

      val graphQlClient = client.graphqlClient

      graphQlClient.query[CreateSeries.CreateSeries.Data, CreateSeries.CreateSeries.Variables](CreateSeries.CreateSeries.document,
        CreateSeries.CreateSeries.Variables(createSeriesInput)).result.map(result => result match {
        case Right(r) => Redirect(routes.CreateConsignmentController.index(r.data.createSeries.id))
        case Left(ex) => InternalServerError(ex.errors.toString())})
    }

    val formValidationResult: Form[CreateSeriesData] = CreateSeriesForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }
}
