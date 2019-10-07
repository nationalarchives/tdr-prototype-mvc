package controllers

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader, Result}

import scala.concurrent.Future

class ErrorRedirectController @Inject () (cc: ControllerComponents) extends AbstractController(cc) with SecuredErrorHandler with I18nSupport {

  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Redirect(routes.AuthController.login()))
  }

  override def onNotAuthorized(implicit request: RequestHeader) = {
    val messages = request.messages
    val message = messages(request.path.replace("/",""))
    Future.successful(Ok(views.html.accessDenied(message)))
  }

}
