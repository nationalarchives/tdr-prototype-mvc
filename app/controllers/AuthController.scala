package controllers

import auth.{CreateUser, User, UserDao}
import javax.inject.{Inject, Singleton}
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.mvc._

import scala.concurrent.ExecutionContext


@Singleton
class AuthController @Inject()(controllerComponents: ControllerComponents, userDao: UserDao)
                              (implicit val executionContext: ExecutionContext)
  extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {


  val form: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply).verifying("Username or password incorrect", u => userDao.verifyUser(u))
  )


  val complexPassword = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"r

  val passwordCheckConstraint: Constraint[String] = Constraint(s"Must match ${complexPassword.pattern}")({ plainText =>
    val errors = plainText match {
      case complexPassword() => Nil
      case _            => Seq(ValidationError("Password does not meet requirements"))
    }
    if (errors.isEmpty) {
      Valid
    } else {
      Invalid(errors)
    }
  })

  val createUserForm: Form[CreateUser] = Form(
    mapping(
      "name" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> text.verifying(passwordCheckConstraint),
      "confirmPassword" -> nonEmptyText
    )(CreateUser.apply)(CreateUser.unapply).verifying("Passwords don't match", c => c.password == c.confirmPassword
    )
  )

  def createUser() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.createUser(createUserForm))
  }

  def createUserComplete() = Action {
    Ok(views.html.createUserComplete())
  }

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(form))
  }

  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  def processCreateUser = Action { implicit request: Request[AnyContent] =>
    val errorFunction: Form[CreateUser] => Result = { formWithErrors: Form[CreateUser] =>
      BadRequest(views.html.createUser(formWithErrors))
    }
    val successFunction: CreateUser => Result = { user: CreateUser =>
      userDao.createUser(user)
      Redirect(routes.AuthController.createUserComplete())
    }
    val formValidationResult: Form[CreateUser] = createUserForm.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )

  }

  def processLoginAttempt = Action{ implicit request: Request[AnyContent] =>
    val errorFunction: Form[User] => Result = { formWithErrors: Form[User] =>
      BadRequest(views.html.login(formWithErrors))
    }
    val successFunction: User => Result = { user: User =>
      Redirect(routes.DashboardController.index())
        .withSession("username" -> user.username)
    }

    val formValidationResult: Form[User] = form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )

  }
}