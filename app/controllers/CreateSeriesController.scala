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

import scala.concurrent.ExecutionContext

@Singleton
class CreateSeriesController @Inject()(
                                            client: GraphQLClientProvider,
                                            controllerComponents: ControllerComponents,
                                            configuration: Configuration)(
                                            implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {
  val createSeriesForm = Form(
    mapping(
      "seriesName" -> text,
      "seriesDescription" -> text
    )(CreateSeriesData.apply)(CreateSeriesData.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.createSeries(createSeriesForm))
  }

  def submit() = Action.async { implicit request: Request[AnyContent] =>

    val seriesData = createSeriesForm.bindFromRequest.get
    val createSeriesInput = CreateSeriesInput(seriesData.seriesName, seriesData.seriesDescription)

    val graphQlClient = client.graphqlClient(List())

    graphQlClient.query[CreateSeries.CreateSeries.Data, CreateSeries.CreateSeries.Variables](CreateSeries.CreateSeries.document,
            CreateSeries.CreateSeries.Variables(createSeriesInput)).result.map(result => result match {
            case Right(r) => Redirect(routes.CreateCollectionController.index(r.data.createSeries.id))
            case Left(ex) => InternalServerError(ex.errors.toString())})
  }
}
