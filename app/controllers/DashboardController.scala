package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class DashboardController @Inject()(
  cc: ControllerComponents,
  authenticatedUserAction: AuthenticatedUserAction
)extends AbstractController(cc) {

  def index() = authenticatedUserAction { implicit request: Request[AnyContent] =>
    Ok(views.html.dashboard())
  }

}
