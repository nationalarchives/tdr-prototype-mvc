package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class CreateSeriesController @Inject()(
                                     cc: ControllerComponents
                                   )extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.createSeries())
  }
}
