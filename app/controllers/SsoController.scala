package controllers

import auth.{DynamoUserService, TdrAuth0Provider}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import javax.inject.Inject
import play.api.mvc.{AnyContent, ControllerComponents, _}
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SsoController @Inject() (
                              components: ControllerComponents,
                              auth0Provider: TdrAuth0Provider,
                              userService: DynamoUserService,
                              authInfoRepository: AuthInfoRepository,
                              silhouette: Silhouette[DefaultEnv],
                              )(
  implicit ex: ExecutionContext
) extends AbstractController(components) {

  def authenticate: Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      auth0Provider.authenticate().flatMap {
        case Left(result) => {
          println("Successful auth result", result)
          Future.successful(result)
        }
        case Right(authInfo) => {
          println("Got auth info", authInfo)
          for {
            profile <- auth0Provider.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            value <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.DashboardController.index()))
          } yield {
            result
          }
        }
      }
    }
  }
}
