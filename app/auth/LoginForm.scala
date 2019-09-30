package auth

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object LoginForm {


  val form: Form[LoginData] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  case class LoginData(username: String, password: String)
}

