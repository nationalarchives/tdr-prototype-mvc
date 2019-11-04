package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.Auth0Provider.ID
import com.mohiva.play.silhouette.impl.providers.oauth2.{Auth0ProfileParser, Auth0Provider, BaseAuth0Provider}
import play.api.libs.json.JsValue

import scala.concurrent.Future

class TdrAuth0Provider(
                        protected val httpLayer: HTTPLayer,
                        protected val stateHandler: SocialStateHandler,
                        val settings: OAuth2Settings
                      ) extends BaseAuth0Provider {

  override type Self = TdrAuth0Provider

  override def withSettings(f: OAuth2Settings => OAuth2Settings) = new TdrAuth0Provider(httpLayer, stateHandler, f(settings))

  override type Profile = CommonSocialProfile

  override protected def profileParser: TdrAuth0ProfileParser = new TdrAuth0ProfileParser
}

class TdrAuth0ProfileParser extends Auth0ProfileParser {
  override def parse(json: JsValue, authInfo: OAuth2Info): Future[CommonSocialProfile] = Future.successful {
    val userID = (json \ "sub").as[String]

    CommonSocialProfile(
      loginInfo = LoginInfo(ID, userID)
    )
  }
}
