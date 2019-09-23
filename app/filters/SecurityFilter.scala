package filters

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.mvc.Results.Unauthorized


import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SecurityFilter @Inject()(
                                bodyParsers: PlayBodyParsers
                              )(
                                implicit val mat: Materializer,
                                implicit val ex: ExecutionContext
                              ) extends Filter {

  def isRequestAuthenticated(request: RequestHeader) = {
    request.session.data.get("username").isDefined
  }

  def apply(next: RequestHeader => Future[Result])(
    request: RequestHeader): Future[Result] = {
    val Assets = "(/assets/.*)".r
    request.path match {
      case "/" | "/login" | "/processLoginAttempt" | Assets(_) => next(request)
      case _ =>
      if(isRequestAuthenticated(request)) {
        next(request)
      } else {
        Future.successful(Unauthorized(views.html.accessDenied(request)))
      }
    }
  }

}
