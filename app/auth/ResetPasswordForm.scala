package auth

import play.api.data.Forms._
import play.api.data.Form
import play.api.data.Forms.mapping
import auth.SignUpForm.passwordCheckConstraint

object ResetPasswordForm {
  val form: Form[Data] = Form(
    mapping(
      "newPassword" -> text.verifying(passwordCheckConstraint),
      "confirmNewPassword" -> text.verifying(passwordCheckConstraint)
    )(Data.apply)(Data.unapply).verifying("Passwords don't match", c => c.newPassword == c.confirmNewPassword)
  )

  case class Data(newPassword: String, confirmNewPassword: String)
}
