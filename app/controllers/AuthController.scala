package controllers

import auth.{User, UserDao}
import javax.inject.{Inject, Singleton}
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import graphql.GraphQLClientProvider
import graphql.codegen.GetConsignments
import javax.inject.{Inject, _}
import play.api.Configuration
import play.api.mvc._


@Singleton
class AuthController @Inject()(controllerComponents: ControllerComponents)(implicit val executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  private val logger = play.api.Logger(this.getClass)

  val form: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText
        .verifying("too few chars", s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 20)),
      "password" -> nonEmptyText
        .verifying("too few chars", s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 30)),
    )(User.apply)(User.unapply)
  )

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(form))
  }

  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  def processLoginAttempt = Action.async { implicit request: Request[AnyContent] =>
    val errorFunction: Form[User] => Future[Result] = { formWithErrors: Form[User] =>
      Future.apply(BadRequest(views.html.login(formWithErrors)))
    }
    val successFunction: User => Future[Result] = { user: User =>
      new UserDao().verifyUser(user.username, user.password).map(verified => {
        if (verified) {
          Redirect(routes.DashboardController.index())
            .flashing("info" -> "You are logged in.")
            .withSession("username" -> user.username)
        } else {
          Redirect(routes.HomeController.index())
            .flashing("info" -> "You are not logged in.")
        }
      })
    }

    val formValidationResult: Form[User] = form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )

  }

  private def lengthIsGreaterThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length > n) true else false
  }

  private def lengthIsLessThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length < n) true else false
  }

}