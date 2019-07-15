package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class DashboardController @Inject()(
  cc: ControllerComponents
)extends AbstractController(cc) {


  val dd = List("aa", "bb", "cc")


  def index() = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.dashboard(dd))
  }
}
