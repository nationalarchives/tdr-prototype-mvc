package auth

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object ResetPasswordEmailForm {
  val form: Form[Data] = Form(
    mapping(
      "email" -> nonEmptyText,
    )(Data.apply)(Data.unapply)
  )

  case class Data(email: String)
}
