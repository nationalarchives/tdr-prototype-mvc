package modules

import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.{KeycloakOidcConfiguration, OidcConfiguration}
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.{DefaultSecurityComponents, Pac4jScalaTemplateHelper, SecurityComponents}
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.play.store.{PlayCacheSessionStore, PlayCookieSessionStore, PlaySessionStore, ShiroAesDataEncrypter}
import play.api.{Configuration, Environment}
import play.cache.SyncCacheApi




class SecurityModule extends AbstractModule {
  override def configure(): Unit = {

    val sKey = System.getenv("PLAY_SECRET_KEY").substring(0, 16)
//    val dataEncrypter = new ShiroAesDataEncrypter(sKey)

//    new PlayCacheSessionStore(getProvider(SyncCacheApi.))
//    val playSessionStore = new PlayCookieSessionStore(dataEncrypter)
//    bind(classOf[PlaySessionStore]).toInstance(playSessionStore)
    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])
    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])

    bind(classOf[Pac4jScalaTemplateHelper[CommonProfile]])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/?defaulturlafterlogout")
    callbackController.setMultiProfile(true)
    callbackController.setRenewSession(false)
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)
  }

  @Provides
  def provideOidcClient: OidcClient[OidcProfile, OidcConfiguration] = {

    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId("tdr")
    oidcConfiguration.setSecret("1c66e726-520f-4c26-a65b-5be292249af8")
    oidcConfiguration.setDiscoveryURI("https://keycloak.tdr-prototype.co.uk/auth/realms/tdr/.well-known/openid-configuration")
    val oidcClient = new OidcClient[OidcProfile, OidcConfiguration](oidcConfiguration)
    oidcClient.setCallbackUrl("http://localhost:9000/callback")
    oidcClient
  }

  @Provides
  def provideConfig(oidcClient: OidcClient[OidcProfile, OidcConfiguration]): Config = {
    val clients = new Clients(oidcClient)

    val config = new Config(clients)
    config.setHttpActionAdapter(new DemoHttpActionAdapter())
    config
  }
}
