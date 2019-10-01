package controllers

import graphql.GraphQLClientProvider
import graphql.codegen.GetConsignment.getConsignment
import graphql.codegen.GetFileStatus.getFileChecksStatus
import javax.inject.{Inject, _}
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.ExecutionContext


@Singleton
class ReviewController @Inject()(
                                  client: GraphQLClientProvider,
                                  controllerComponents: ControllerComponents,
                                  configuration: Configuration)(
                                  implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  def index(consignmentId: Int) = Action.async {
    implicit request =>

      val appSyncClient = client.graphqlClient(List())

      val consignmentDetails =  appSyncClient.query[getConsignment.Data, getConsignment.Variables](getConsignment.document, getConsignment.Variables(consignmentId)).result
      val fileDetails = appSyncClient.query[getFileChecksStatus.Data, getFileChecksStatus.Variables](getFileChecksStatus.document, getFileChecksStatus.Variables(consignmentId)).result

      for {
        consignment <-consignmentDetails
        files <- fileDetails
      } yield {
        Ok(views.html.review(consignment.right.get.data.getConsignment.get, files.right.get.data.getFileChecksStatus))
      }
  }
}
