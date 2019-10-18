package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, number}

object CommenceUploadForm {
  val form: Form[CommenceUploadData] = Form(
    mapping(
      "numberOfFiles" -> number
        .verifying("You must select one or more files for upload", n => n > 0)
    )(CommenceUploadData.apply)(CommenceUploadData.unapply)
  )

  case class CommenceUploadData(numberOfFiles: Int)
}
