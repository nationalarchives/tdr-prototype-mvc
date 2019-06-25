package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class UploadController @Inject()(
  controllerComponents: ControllerComponents
) extends AbstractController(controllerComponents) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.upload())
  }
}
