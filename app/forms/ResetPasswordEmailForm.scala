package forms

import play.api.data.Form
import play.api.data.Forms.{mapping,  email}

object ResetPasswordEmailForm {
  val form: Form[Data] = Form(
    mapping(
      "email" -> email,
    )(Data.apply)(Data.unapply)
  )

  case class Data(email: String)
}
