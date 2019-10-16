package forms

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ServiceAgreementsForm {

  val form: Form[ServiceAgreementsData] = Form(
    mapping(
      "publicRecord" -> boolean
        .verifying("Must answer yes", b => b),
      "crownCopyright" -> boolean
        .verifying("Must answer yes", b => b),
      "english" -> boolean
        .verifying("Must answer yes", b => b),
      "digital" -> boolean
        .verifying("Must answer yes", b => b),
      "droAppraisalselection" -> boolean
        .verifying("Must answer yes", b => b),
      "droSensitivity" -> boolean
        .verifying("Must answer yes", b => b),
    )(ServiceAgreementsData.apply)(ServiceAgreementsData.unapply)
  )

  case class ServiceAgreementsData (publicRecord: Boolean,
                                    crownCopyright: Boolean,
                                    english: Boolean,
                                    digital: Boolean,
                                    droAppraisalselection: Boolean,
                                    droSensitivity: Boolean)

}
