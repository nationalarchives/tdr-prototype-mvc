package filters

import akka.stream.Materializer
import controllers.routes
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.mvc.Results.{Ok, Unauthorized, Redirect}


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
      case "/" =>
        if(isRequestAuthenticated(request)) {
          Future.successful(Ok(views.html.dashboard()))
        }
        Future.successful(Redirect(routes.AuthController.login()))
      case "/login" | "/processLoginAttempt" | Assets(_) =>
        next(request)
      case _ =>
      if(isRequestAuthenticated(request)) {
        next(request)
      } else {
        Future.successful(Redirect(routes.AuthController.login()))
      }
    }
  }

}
