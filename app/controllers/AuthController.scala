package controllers

import auth.{DefaultEnv, UserService}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import javax.inject.Inject
import play.api.mvc.{ControllerComponents, _}
import providers.CognitoProvider

import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject() (
                                 components: ControllerComponents,
                                 silhouette: Silhouette[DefaultEnv],
                                 userService: UserService,
                                 authInfoRepository: AuthInfoRepository,
                                 cognitoProvider: CognitoProvider
)(
  implicit ex: ExecutionContext
) extends AbstractController(components) {

  def authenticate = Action.async { implicit request: Request[AnyContent] =>
    cognitoProvider.authenticate().flatMap {
      case Left(result) => Future.successful(result)
      case Right(authInfo) => {
        for {
          profile <- cognitoProvider.retrieveProfile(authInfo)
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
