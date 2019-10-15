package controllers

import com.mohiva.play.silhouette.api.Silhouette
import graphql.GraphQLClientProvider
import graphql.codegen.CreateConsignment.createConsignment
import javax.inject.{Inject, _}
import model.CreateConsignmentData
import play.api.Configuration
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

    Future(Ok(views.html.createConsignments(CreateConsignmentData.form, seriesId)))
  }

  def submit() = silhouette.UserAwareAction.async { implicit request =>
    request.identity.map(id => {
      val graphQlClient = client.graphqlClient
      val form = CreateConsignmentData.form.bindFromRequest
      val vars = createConsignment.Variables(form.get.consignmentName,form.get.seriesId,id.email,form.get.transferringBody)
      graphQlClient.query[createConsignment.Data, createConsignment.Variables](createConsignment.document, vars).result.map {
        case Right(r) =>
          Redirect(routes.UploadController.index(r.data.createConsignment.id, r.data.createConsignment.series.id))
        case Left(e) => InternalServerError(e.errors.toString())
      }
    }).get
  }
}
