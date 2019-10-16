package controllers

import com.mohiva.play.silhouette.api.Silhouette
import forms.CreateConsignmentForm
import forms.CreateConsignmentForm.CreateConsignmentData
import forms.CreateSeriesForm.CreateSeriesData
import forms.SelectedSeriesForm.SelectedSeriesData
import graphql.GraphQLClientProvider
import graphql.codegen.CreateConsignment.createConsignment
import javax.inject.{Inject, _}
import play.api.Configuration
import play.api.data.Form
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateConsignmentController @Inject()(
                                             client: GraphQLClientProvider,
                                             controllerComponents: ControllerComponents,
                                             silhouette: Silhouette[DefaultEnv],
                                             configuration: Configuration)(
                                             implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {



  def index(seriesId:Int) = silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>

    Future(Ok(views.html.createConsignments(CreateConsignmentForm.form, seriesId)))
  }

  def submit() = silhouette.UserAwareAction.async { implicit request =>

    val errorFunction: Form[CreateConsignmentData] => Future[Result] = { formWithErrors: Form[CreateConsignmentData] =>

        Future.successful(BadRequest(views.html.createConsignments(formWithErrors, formWithErrors.get.seriesId))
      )
    }

    val successFunction: CreateConsignmentData => Future[Result] = { data: CreateConsignmentData =>

      request.identity.map(id => {
        val graphQlClient = client.graphqlClient
        val vars = createConsignment.Variables(data.consignmentName, data.seriesId, id.email, data.transferringBody)
        graphQlClient.query[createConsignment.Data, createConsignment.Variables](createConsignment.document, vars).result.map {
          case Right(r) =>
            Redirect(routes.UploadController.index(r.data.createConsignment.id, r.data.createConsignment.series.id))
          case Left(e) => InternalServerError(e.errors.toString())
        }

      }).get
    }

    val formValidationResult: Form[CreateConsignmentData] = CreateConsignmentForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }
}
