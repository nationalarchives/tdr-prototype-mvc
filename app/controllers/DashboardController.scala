package controllers

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject._
import play.api.mvc._

import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class DashboardController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
)extends AbstractController(cc) {

  def index() = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.dashboard()))
  }

}
