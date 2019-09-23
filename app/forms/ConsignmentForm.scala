package forms

import play.api.data.Form
import play.api.data.Forms._

case class ConsignmentForm(consignmentName: String, transferringBody: String, series: Int)

object ConsignmentForm {
  val form: Form[ConsignmentForm] = Form(
    mapping(
      "consignmentName" -> text,
      "transferringBody" -> text,
      "series" -> number
    )(ConsignmentForm.apply)(ConsignmentForm.unapply)
  )
}

