package controllers

import javax.inject._
import model.ServiceAgreementsData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceAgreementsController @Inject()(controllerComponents: ControllerComponents,
                                             configuration: Configuration)
                                           (implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  private val options: Seq[(String, String)] = Seq("Yes" -> "yes", "No" -> "no")

  val form = Form(
    mapping(
      "publicRecord" -> text
        .verifying("Must answer yes", s => hasAgreed(s)),
      "crownCopyright" -> text
        .verifying("Must answer yes", s => hasAgreed(s)),
      "english" -> text
        .verifying("Must answer yes", s => hasAgreed(s)),
      "digital" -> text
        .verifying("Must answer yes", s => hasAgreed(s)),
      "droAppraisalselection" -> text
        .verifying("Must answer yes", s => hasAgreed(s)),
      "droSensitivity" -> text
        .verifying("Must answer yes", s => hasAgreed(s)),
    )(ServiceAgreementsData.apply)(ServiceAgreementsData.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.serviceAgreements(form, options))
  }

  //Only print information to console to show form works
  def submit() = Action.async { implicit request: Request[AnyContent] =>

    val errorFunction: Form[ServiceAgreementsData] => Future[Result] = { formWithErrors: Form[ServiceAgreementsData] =>
      Future.apply(BadRequest(views.html.serviceAgreements(formWithErrors, options)))
    }
    val successFunction: ServiceAgreementsData => Future[Result] = { formData: ServiceAgreementsData =>
      println("++++SERVICE AGREEMENT START++++")
      println("Public Record: " + formData.publicRecord)
      println("Crown Copyright: " + formData.crownCopyright)
      println("English Language: " + formData.english)
      println("Digital: " + formData.digital)
      println("DRO Appraisal: " + formData.droAppraisalselection)
      println("DRO Sensitivity: " + formData.droSensitivity)
      println("++++SERVICE AGREEMENT END++++")

      Future.apply(Redirect(routes.SeriesDetailsController.index()))
    }

    val formValidationResult: Form[ServiceAgreementsData] = form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  private def hasAgreed(s: String): Boolean = {
    s.equals("yes")
  }
}
