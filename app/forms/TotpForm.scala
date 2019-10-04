package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

/**
  * The form which handles the submission of the credentials plus verification code for TOTP-authentication
  */
object TotpForm {
  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "userID" -> nonEmptyText,
      "sharedKey" -> nonEmptyText,
      "verificationCode" -> nonEmptyText(minLength = 6, maxLength = 6)
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    * @param userID The unique identifier of the user.
    * @param sharedKey the TOTP shared key
    * @param verificationCode Verification code for TOTP-authentication
    */
  case class Data(
                   userID: String,
                   sharedKey: String,
                   verificationCode: String = "")
}
