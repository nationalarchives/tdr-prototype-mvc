package controllers

import javax.inject._
import models.{User}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class UserController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  val form: Form[User] = Form (
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  private val formSubmitUrl = routes.UserController.processLoginAttempt

  def loginForm() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.login(form, formSubmitUrl))
  }

  def processLoginAttempt = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[User] =>
      // form validation/binding failed...
      BadRequest(views.html.login(formWithErrors, formSubmitUrl))
    }
    val successFunction = { user: User =>
      // form validation/binding succeeded ...
      val foundUser: Boolean = true
      if (foundUser) {
        Redirect(routes.DashboardController.index())
          .flashing("info" -> "You are logged in.")
          .withSession("username" -> user.username)
      } else {
        Redirect(routes.UserController.loginForm())
          .flashing("error" -> "Invalid username/password.")
      }
    }
    val formValidationResult: Form[User] = form.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

}
