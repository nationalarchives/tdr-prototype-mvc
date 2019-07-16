package controllers

import graphQL.TDRGraphQLClient
import javax.inject._
import play.api.Configuration
import play.api.mvc._
import model.TdrCollection

@Singleton
class ViewCollectionsController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration) extends AbstractController(controllerComponents)
{
  def index() = Action { implicit request: Request[AnyContent] =>
    val apiUrl = "https://rn9sl8cy7f.execute-api.eu-west-2.amazonaws.com/dev"

    val collection1 = new TdrCollection(
      "Id1",
      "Name 1",
      "Crown Copyright",
      "true",
      "Legal Status")

    val collection2 = new TdrCollection(
      "Id2",
      "Name 2",
      "Crown Copyright",
      "true",
      "Legal Status")

    val collections: List[TdrCollection]  = List(
     collection1,
     collection2)

    Ok(views.html.getCollection(collections))
  }
}
