package forms

import SignUpForm.passwordCheckConstraint
import play.api.data.Form
import play.api.data.Forms.{mapping, text, email}

object LoginForm {


  val form: Form[LoginData] = Form(
    mapping(
      "username" -> email,
      "password" -> text.verifying(passwordCheckConstraint)
    )(LoginData.apply)(LoginData.unapply)
  )

  case class LoginData(username: String, password: String)
}
