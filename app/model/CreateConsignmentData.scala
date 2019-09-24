package model

import play.api.data.Form
import play.api.data.Forms._

case class CreateConsignmentData(consignmentName: String, transferringBody: String, seriesId: Int)

object CreateConsignmentData {
  val form: Form[CreateConsignmentData] = Form(
    mapping(
      "consignmentName" -> text,
      "transferringBody" -> text,
      "seriesId" -> number
    )(CreateConsignmentData.apply)(CreateConsignmentData.unapply)
  )
}

