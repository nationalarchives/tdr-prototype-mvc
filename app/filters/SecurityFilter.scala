package filters

import akka.stream.Materializer
import auth.{DefaultEnv, DynamoUserService}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import javax.inject.{Inject, Singleton}
import play.api.mvc.Results.Unauthorized
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SecurityFilter @Inject()(
  silhouette: Silhouette[DefaultEnv],
  bodyParsers: PlayBodyParsers,
  userService: DynamoUserService
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
          case true => next(request)
          case false => Future.successful(Unauthorized(views.html.accessDenied(request)))
        }
      }
    }

    action(request).run
  }

  private def isRequestAuthenticated(request: UserAwareRequest[DefaultEnv, Unit]): Future[Boolean] = request.identity match {
    case Some(user) => userService.retrieve(user.loginInfo).map(u => u.nonEmpty)
    case None => Future.successful(false)
  }
}
