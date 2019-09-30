package controllers

import auth.Authorisers.IsSeriesCreator
import auth._
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import graphql.GraphQLClientProvider
import graphql.codegen.CreateConsignment.createConsignment
import javax.inject.{Inject, _}
import model.CreateConsignmentData
import play.api.Configuration
import play.api.mvc.{request, _}
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateConsignmentController @Inject()(
                                             client: GraphQLClientProvider,
                                             controllerComponents: ControllerComponents,
                                             silhouette: Silhouette[DefaultEnv],
                                             configuration: Configuration,
                                             isSeriesCreator: IsSeriesCreator)(
                                             implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {



  def index(seriesId:Int) = silhouette.SecuredAction(isSeriesCreator).async { implicit request: Request[AnyContent] =>

    Future(Ok(views.html.createConsignments(CreateConsignmentData.form, seriesId)))
  }

  def submit() = silhouette.UserAwareAction.async { implicit request =>
    request.identity.map(id => {
      val appSyncClient = client.graphqlClient(List())
      val form = CreateConsignmentData.form.bindFromRequest
      val vars = createConsignment.Variables(form.get.consignmentName,form.get.seriesId,id.email,form.get.transferringBody)
      appSyncClient.query[createConsignment.Data, createConsignment.Variables](createConsignment.document, vars).result.map {
        case Right(r) => {
          Redirect(routes.UploadController.index(r.data.createConsignment.id))
        }
        case Left(ex) => InternalServerError(ex.errors.toString())
      }
    }).get


  }


}
