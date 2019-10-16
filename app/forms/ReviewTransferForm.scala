package forms

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ReviewTransferForm {

  val form:Form[ReviewTransferData] = Form(
    mapping(
      "confirmRecordTransfer" -> boolean
        .verifying("Must answer yes", b => b),
      "confirmOpen" -> boolean
        .verifying("Must answer yes", b => b),
      "confirmTnaOwnership" -> boolean
        .verifying("Must answer yes", b => b),
    )(ReviewTransferData.apply)(ReviewTransferData.unapply)
  )

  case class ReviewTransferData(confirmRecordTransfer: Boolean, confirmOpen: Boolean, confirmTnaOwnership: Boolean)
}
