package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
)(
  implicit ex: ExecutionContext
) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = silhouette.UserAwareAction { implicit request =>

    Ok(views.html.index())
  }
}
