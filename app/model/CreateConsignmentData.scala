package forms

import play.api.data.Form
import play.api.data.Forms._

case class ConsignmentData(consignmentName: String, transferringBody: String, seriesId: Int)

object ConsignmentData {
  val form: Form[ConsignmentData] = Form(
    mapping(
      "consignmentName" -> text,
      "transferringBody" -> text,
      "seriesId" -> number
    )(ConsignmentData.apply)(ConsignmentData.unapply)
  )
}

