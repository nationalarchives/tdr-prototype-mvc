package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api.mvc._
import utils.DefaultEnv

@Singleton
class DashboardController @Inject()(
  silhouette: Silhouette[DefaultEnv],
  cc: ControllerComponents
)extends AbstractController(cc) {


  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.dashboard())
  }
}
