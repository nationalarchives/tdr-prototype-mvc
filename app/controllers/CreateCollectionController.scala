package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext

@Singleton
class CreateCollectionController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration)(
  implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.createCollection())
  }

}
