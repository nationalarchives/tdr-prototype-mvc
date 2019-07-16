package filters

import akka.actor.Status.Success
import akka.stream.Materializer
import auth.{DefaultEnv, DynamoUserService, User}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import javax.inject.{Inject, Singleton}
import modules.TypedKeys
import play.api.libs.typedmap.TypedKey
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
        case "/" | "/authenticate/cognito" | Assets(_) => next(request)
        case _ => isRequestAuthenticated(r).flatMap {
          case Some(user) => {
            authInfoRepository.find[OAuth2Info](user.loginInfo).flatMap {
              case Some(p) => {
                next(request.addAttr(TypedKeys.oAuth2Info, p))
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
