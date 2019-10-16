package controllers

import forms.ServiceAgreementsForm
import forms.ServiceAgreementsForm.ServiceAgreementsData
import javax.inject._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ServiceAgreementsController @Inject()(controllerComponents: ControllerComponents,
                                             configuration: Configuration)
                                           (implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  private val options: Seq[(String, String)] = Seq("Yes" -> "true", "No" -> "false")

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.serviceAgreements(ServiceAgreementsForm.form, options))
  }

  //Only print information to console to show form works
  def submit() = Action { implicit request: Request[AnyContent] =>

    val errorFunction: Form[ServiceAgreementsData] => Result = { formWithErrors: Form[ServiceAgreementsData] =>
      BadRequest(views.html.serviceAgreements(formWithErrors, options))
    }
    val successFunction: ServiceAgreementsData => Result = { formData: ServiceAgreementsData =>
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

    val formValidationResult: Form[ServiceAgreementsData] = ServiceAgreementsForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }
}
