package controllers

import graphql.GraphQLClientProvider
import graphql.codegen.CreateConsignment.createConsignment._
import javax.inject.{Inject, _}
import model.CreateConsignmentData
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateConsignmentController @Inject()(
                                             client: GraphQLClientProvider,
                                             controllerComponents: ControllerComponents,
                                             configuration: Configuration)(
                                             implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  def index(seriesId:Int) = Action.async { implicit request: Request[AnyContent] =>
    Future(Ok(views.html.createConsignments(CreateConsignmentData.form, seriesId)))
  }

  def submit() = Action.async { implicit request =>

    val appSyncClient = client.graphqlClient(List())
    val form = CreateConsignmentData.form.bindFromRequest

    val vars = Variables(form.get.consignmentName,form.get.seriesId,request.session.get("username").get,form.get.transferringBody)
    appSyncClient.query[Data,Variables](document, vars).result.map(result => result match {
      case Right(r) => {
        Redirect(routes.UploadController.index(r.data.createConsignment.id))
      }
      case Left(ex) => InternalServerError(ex.errors.toString())
    })
  }
}
