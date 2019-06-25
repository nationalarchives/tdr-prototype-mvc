package controllers

import javax.inject._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.Configuration
import play.api.mvc._

@Singleton
class UploadController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration
) extends AbstractController(controllerComponents) {

  def index() = Action { implicit request: Request[AnyContent] =>
    val codeOption = request.queryString.get("code").flatMap(_.headOption)

    val cognitoConfig = configuration.underlying.as[CognitoUploadConfig]("cognito.upload")

    // TODO: Add state parameter? How do we validate it?
    val url = s"${cognitoConfig.authenticationUrl}?response_type=code&scope=${cognitoConfig.scopes}&client_id=${cognitoConfig.clientId}&redirect_uri=${cognitoConfig.redirectUrl}"

    codeOption match {
      case Some(code) => Ok(views.html.upload())
      case None => Redirect(url)
    }
  }
}

case class CognitoUploadConfig(
  authenticationUrl: String,
  redirectUrl: String,
  scopes: String,
  clientId: String)
