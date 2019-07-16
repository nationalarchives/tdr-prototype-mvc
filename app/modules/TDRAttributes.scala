package modules

import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import play.api.libs.typedmap.TypedKey

object TDRAttributes {
  val OAuthAccessTokenKey: TypedKey[OAuth2Info] = TypedKey[OAuth2Info]("accessTokens")
}
