package controllers

import akka.http.scaladsl.model.headers.RawHeader
import graphql.GraphQLClientProvider
import javax.inject._
import model.CreateSeriesData
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import graphql.codegen.CreateSeries
import graphql.codegen.types.CreateSeriesInput
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateSeriesController @Inject()(
                                            client: GraphQLClientProvider,
                                            controllerComponents: ControllerComponents,
                                            configuration: Configuration)(
                                            implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {
  val createSeriesForm = Form(
    mapping(
      "seriesName" -> nonEmptyText,
      "seriesDescription" -> text
    )(CreateSeriesData.apply)(CreateSeriesData.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.createSeries(createSeriesForm))
  }

  def submit() = Action.async { implicit request: Request[AnyContent] =>

    val errorFunction: Form[CreateSeriesData] => Future[Result] = { formWithErrors: Form[CreateSeriesData] =>
      Future.apply(BadRequest(views.html.createSeries(formWithErrors)))
    }
    val successFunction: CreateSeriesData => Future[Result] = { data: CreateSeriesData =>
      val createSeriesInput = CreateSeriesInput(data.seriesName, data.seriesDescription)

      val graphQlClient = client.graphqlClient

      graphQlClient.query[CreateSeries.CreateSeries.Data, CreateSeries.CreateSeries.Variables](CreateSeries.CreateSeries.document,
        CreateSeries.CreateSeries.Variables(createSeriesInput)).result.map(result => result match {
        case Right(r) => Redirect(routes.CreateConsignmentController.index(r.data.createSeries.id))
        case Left(ex) => InternalServerError(ex.errors.toString())})
    }

    val formValidationResult: Form[CreateSeriesData] = createSeriesForm.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }
}
