package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, text}

object CreateSeriesForm {

  val form: Form[CreateSeriesData] = Form(
    mapping(
      "seriesName" -> nonEmptyText,
      "seriesDescription" -> text
    )(CreateSeriesData.apply)(CreateSeriesData.unapply)
  )

  case class CreateSeriesData (seriesName: String, seriesDescription: String)
}
