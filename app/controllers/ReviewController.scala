package controllers

import com.mohiva.play.silhouette.api.Silhouette
import graphql.GraphQLClientProvider
import graphql.codegen.ConfirmTransfer.confirmTransfer
import graphql.codegen.ConfirmTransfer.confirmTransfer.{Data, Variables}
import graphql.codegen.GetConsignment.getConsignment
import graphql.codegen.GetConsignment.getConsignment._
import graphql.codegen.GetFileStatus.getFileChecksStatus
import graphql.codegen.GetFileStatus.getFileChecksStatus.GetFileChecksStatus
import javax.inject.{Inject, _}
import model.ReviewData
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, text, boolean}
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ReviewController @Inject()(
                                  client: GraphQLClientProvider,
                                  controllerComponents: ControllerComponents,
                                  silhouette: Silhouette[DefaultEnv],
                                  configuration: Configuration)(
                                  implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  val form = Form(
    mapping(
      "confirmRecordTransfer" -> boolean,
      "confirmOpen" -> boolean,
      "confirmTnaOwnership" -> boolean,
    )(ReviewData.apply)(ReviewData.unapply)
  )

  def index(consignmentId: Int) = silhouette.SecuredAction.async {
    implicit request =>
      getDetailsToReview(consignmentId).map(reviewDetails =>
        Ok(views.html.review(reviewDetails.consignment, reviewDetails.fileChecks, form))
      )
  }

  def submit(consignmentId: Int) = silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>

    val errorFunction: Form[ReviewData] => Future[Result] = { formWithErrors: Form[ReviewData] =>
      getDetailsToReview(consignmentId).map(reviewDetails =>
        BadRequest(views.html.review(reviewDetails.consignment, reviewDetails.fileChecks, formWithErrors))
      )
    }

    val successFunction: ReviewData => Future[Result] = { formData: ReviewData =>
      val graphQlClient = client.graphqlClient
      val vars = Variables(consignmentId)

      graphQlClient.query[Data, Variables](confirmTransfer.document, vars).result.map {
        case Right(_) => {
          Redirect(routes.TransferConfirmationController.index())
        }
        case Left(ex) => InternalServerError(ex.errors.toString())
      }
    }

    val formValidationResult: Form[ReviewData] = form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  private def hasAgreed(s: String): Boolean = {
    s.equals("yes")
  }

  private def getDetailsToReview(consignmentId: Int): Future[ConsignmentReview] = {
    val graphQlClient = client.graphqlClient

    val consignmentDetails =  graphQlClient.query[getConsignment.Data, getConsignment.Variables](getConsignment.document, getConsignment.Variables(consignmentId)).result
    val fileDetails = graphQlClient.query[getFileChecksStatus.Data, getFileChecksStatus.Variables](getFileChecksStatus.document, getFileChecksStatus.Variables(consignmentId)).result

    for {
      consignment <- consignmentDetails
      files <- fileDetails
    } yield {
      ConsignmentReview(consignment.right.get.data.getConsignment.get, files.right.get.data.getFileChecksStatus)
    }
  }

  case class ConsignmentReview(consignment: GetConsignment, fileChecks: GetFileChecksStatus)
}
