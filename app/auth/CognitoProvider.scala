package providers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.{GetUserRequest, GetUserResponse}

import scala.concurrent.Future

class CognitoProvider(
                       protected val httpLayer: HTTPLayer,
                       val stateHandler: SocialStateHandler,
                       val settings: OAuth2Settings) extends OAuth2Provider {

  override type Self = CognitoProvider

  override def withSettings(f: OAuth2Settings => OAuth2Settings) = new CognitoProvider(httpLayer, stateHandler, f(settings))

  override val id: String = CognitoProvider.id

  override type Content = GetUserResponse
  override type Profile = CognitoSocialProfile

  override protected def urls: Map[String, String] = Map()

  override protected def buildProfile(authInfo: OAuth2Info): Future[CognitoSocialProfile] = {
    val providerClient = CognitoIdentityProviderClient.builder
      .region(Region.EU_WEST_2)
      .build
    val getUserRequest = GetUserRequest.builder().accessToken(authInfo.accessToken).build

    val userResult: GetUserResponse = providerClient.getUser(getUserRequest)

    profileParser.parse(userResult, authInfo)
  }

  override protected def profileParser: SocialProfileParser[GetUserResponse, CognitoSocialProfile, OAuth2Info] = new CognitoProfileParser
}

object CognitoProvider {
  val id = "cognito"
}

case class CognitoSocialProfile(
  loginInfo: LoginInfo
) extends SocialProfile

class CognitoProfileParser extends SocialProfileParser[GetUserResponse, CognitoSocialProfile, OAuth2Info] {
  override def parse(content: GetUserResponse, authInfo: OAuth2Info): Future[CognitoSocialProfile] = {
    Future.successful(CognitoSocialProfile(
      LoginInfo(CognitoProvider.id, content.username)
    ))
  }
}
