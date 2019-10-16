package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, number}

object SelectedSeriesForm {

  val form: Form[SelectedSeriesData] = Form(
    mapping(
      "seriesNo" -> number
    )(SelectedSeriesData.apply)(SelectedSeriesData.unapply)
  )

  case class SelectedSeriesData (seriesId: Int)
}
