package controllers

import javax.inject._
import model.ServiceAgreementsData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ServiceAgreementsController @Inject()(controllerComponents: ControllerComponents,
                                             configuration: Configuration)
                                           (implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {


//  private val questions: Seq[(String, String)] = Seq(
//    "The records are all public records" -> "publicRecords",
//    "The records are all Crown Copyright" -> "crownCopyright",
//    "The records are all in English" -> "english",
//    "The  records are all Digital" -> "digital")
  private val options: Seq[(String, String)] = Seq("Yes" -> "true", "No" -> "false")

  val form = Form(
    mapping(
      "publicRecord" -> text,
      "crownCopyright" -> text,
      "english" -> text,
      "digital" -> text
    )(ServiceAgreementsData.apply)(ServiceAgreementsData.unapply)
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.serviceAgreements(form, options))
  }

  //Only print information to console to show form works
  def submit() = Action { implicit request: Request[AnyContent] =>
    val formData = form.bindFromRequest.get
    println("++++SERVICE AGREEMENT START++++")
    println("Public Record: " + formData.publicRecord)
    println("Crown Copyright: " + formData.crownCopyright)
    println("English Language: " + formData.english)
    println("Digital: " + formData.digital)
    println("++++SERVICE AGREEMENT END++++")

    Redirect(routes.SeriesDetailsController.index())
  }
}
