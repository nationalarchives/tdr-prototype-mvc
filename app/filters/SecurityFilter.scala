package filters

import akka.stream.Materializer
import auth.{DefaultEnv, DynamoUserService, User}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import javax.inject.{Inject, Singleton}
import modules.TDRAttributes
import play.api.mvc.Results.Unauthorized
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SecurityFilter @Inject()(
                                silhouette: Silhouette[DefaultEnv],
                                bodyParsers: PlayBodyParsers,
                                userService: DynamoUserService,
                                authInfoRepository: AuthInfoRepository
                              )(
                                implicit val mat: Materializer,
                                implicit val ex: ExecutionContext
                              ) extends Filter {

  def apply(next: RequestHeader => Future[Result])(

    request: RequestHeader): Future[Result] = {

    val action = silhouette.UserAwareAction.async(bodyParsers.empty) { r =>

      val Assets = "(/assets/.*)".r

      request.path match {
        case "/" | "/filedata" | "/authenticate/cognito" | Assets(_) => next(request)
        case _ => isRequestAuthenticated(r).flatMap {
          case Some(user) => {

            val oauth2InfoOption: Future[Option[OAuth2Info]] = authInfoRepository.find[OAuth2Info](user.loginInfo)
            oauth2InfoOption.flatMap {
              case Some(someToken) => {
                 next(request.addAttr(TDRAttributes.OAuthAccessTokenKey, someToken))
              }
              case _ => next(request)
            }
          }
          case _ => Future.successful(Unauthorized(views.html.accessDenied(request)))
        }
      }
    }

    action(request).run
  }

  private def isRequestAuthenticated(request: UserAwareRequest[DefaultEnv, Unit]): Future[Option[User]] = request.identity match {
    case Some(user) => userService.retrieve(user.loginInfo)
    case None => Future.successful(None)
  }
}
