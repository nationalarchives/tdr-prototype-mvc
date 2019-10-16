package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}

object CreateConsignmentForm {

  val form: Form[CreateConsignmentData] = Form(
    mapping(
      "consignmentName" -> text,
      "transferringBody" -> text,
      "seriesId" -> number
    )(CreateConsignmentData.apply)(CreateConsignmentData.unapply)
  )

  case class CreateConsignmentData(consignmentName: String, transferringBody: String, seriesId: Int)
}
