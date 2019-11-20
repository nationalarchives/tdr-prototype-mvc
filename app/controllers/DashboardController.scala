package controllers

import javax.inject.{Inject, _}
import org.pac4j.core.profile._
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala._
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.credentials.authenticator.UserInfoOidcAuthenticator

@Singleton
class DashboardController @Inject() (sessionStore: PlaySessionStore, val controllerComponents: SecurityComponents, implicit val pac4jTemplateHelper: Pac4jScalaTemplateHelper[CommonProfile]) extends Security[CommonProfile] {


  def index = Secure("OidcClient") { implicit request: Request[AnyContent] =>
    Ok(views.html.dashboard())
  }

}
