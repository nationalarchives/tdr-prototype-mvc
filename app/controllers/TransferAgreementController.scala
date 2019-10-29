package controllers

import javax.inject._
import model.TransferAgreementData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class TransferAgreementController @Inject()(controllerComponents: ControllerComponents,
                                             configuration: Configuration)
                                           (implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  private val options: Seq[(String, String)] = Seq("Yes" -> "true", "No" -> "false")

  val form = Form(
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
    )(TransferAgreementData.apply)(TransferAgreementData.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.transferAgreement(form, options))
  }

  //Only print information to console to show form works
  def submit() = Action { implicit request: Request[AnyContent] =>

    val errorFunction: Form[TransferAgreementData] => Result = { formWithErrors: Form[TransferAgreementData] =>
      BadRequest(views.html.transferAgreement(formWithErrors, options))
    }
    val successFunction: TransferAgreementData => Result = { formData: TransferAgreementData =>
      println("++++SERVICE AGREEMENT START++++")
      println("Public Record: " + formData.publicRecord)
      println("Crown Copyright: " + formData.crownCopyright)
      println("English Language: " + formData.english)
      println("Digital: " + formData.digital)
      println("DRO Appraisal: " + formData.droAppraisalselection)
      println("DRO Sensitivity: " + formData.droSensitivity)
      println("++++SERVICE AGREEMENT END++++")

      Redirect(routes.SeriesDetailsController.index())
    }

    val formValidationResult: Form[TransferAgreementData] = form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }
}
