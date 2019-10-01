package auth

import play.api.data.Forms._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import auth.SignUpForm.passwordCheckConstraint

object LoginForm {


  val form: Form[LoginData] = Form(
    mapping(
      "username" -> email,
      "password" -> text.verifying(passwordCheckConstraint)
    )(LoginData.apply)(LoginData.unapply)
  )

  case class LoginData(username: String, password: String)
}

