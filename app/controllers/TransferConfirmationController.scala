package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

@Singleton
class TransferConfirmationController @Inject()(
                                                cc: ControllerComponents
                                              )extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.transferConfirmation())
  }
}
