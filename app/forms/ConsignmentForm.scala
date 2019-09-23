package forms

import play.api.data.Form
import play.api.data.Forms._

case class ConsignmentForm(consignmentName: String, transferringBody: String, seriesId: Int)

object ConsignmentForm {
  val form: Form[ConsignmentForm] = Form(
    mapping(
      "consignmentName" -> text,
      "transferringBody" -> text,
      "seriesId" -> number
    )(ConsignmentForm.apply)(ConsignmentForm.unapply)
  )
}

