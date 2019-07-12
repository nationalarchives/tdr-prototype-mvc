package controllers

import javax.inject._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.Configuration
import play.api.mvc._
import software.amazon.awssdk.services.appsync.AppSyncClient

@Singleton
class ViewCollectionController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration) extends AbstractController(controllerComponents)
{
  def index() = Action { implicit request: Request[AnyContent] =>
    val apiUrl = "https://rn9sl8cy7f.execute-api.eu-west-2.amazonaws.com/dev"



    Ok(views.html.getCollection())

  }
}

case class CognitoGetCollectionConfig(
  authenticationUrl: String,
  redirectUrl: String,
  scopes: String,
  clientId: String)
