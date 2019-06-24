package filters

import akka.stream.Materializer
import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.{Inject, Singleton}
import play.api.mvc.Results.Unauthorized
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class SecurityFilter @Inject() (silhouette: Silhouette[DefaultEnv], bodyParsers: PlayBodyParsers)(implicit val mat: Materializer) extends Filter {

  def apply(next: RequestHeader => Future[Result])(

    request: RequestHeader): Future[Result] = {

    val action = silhouette.UserAwareAction.async(bodyParsers.empty) { r =>

      val Assets = "(/assets/.*)".r

      request.path match {
        case "/" | "/authenticate/cognito" | Assets(_) =>   next(request)
        case _@ tx  if r .identity.isEmpty =>  Future.successful(Unauthorized(views.html.accessDenied(request)))
        case _@ tx =>  next(request)
       }
    }

    action(request).run
  }

}
