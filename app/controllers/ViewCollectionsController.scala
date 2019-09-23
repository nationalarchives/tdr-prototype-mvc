package controllers

import akka.http.scaladsl.model.headers.RawHeader
import forms.ConsignmentForm
import graphql.GraphQLClientProvider
import graphql.codegen.GetConsignments
import javax.inject.{Inject, _}
import modules.TDRAttributes
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewCollectionsController @Inject()(
                                           client: GraphQLClientProvider,
                                           controllerComponents: ControllerComponents,
                                           configuration: Configuration)(
                                           implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) with play.api.i18n.I18nSupport {

  def index() = Action.async { implicit request: Request[AnyContent] =>

    val accessToken = request.attrs.get(TDRAttributes.OAuthAccessTokenKey).get.accessToken
    val header = RawHeader("Authorization", accessToken)

    val appSyncClient = client.graphqlClient(List(header))

    //        appSyncClient.query[CreateConsignment.createCon.Data, CreateConsignment.createCon.Variables](CreateConsignment.createCon.document,
    //          CreateConsignment.createCon.Variables("drunkTest", 4)).result.map(result => result match {
    //          case Right(r) => println("Saved")
    //          case Left(ex) => println(ex)
    //
    //        })


    Future(Ok(views.html.consignments(ConsignmentForm.form)))

//    appSyncClient.query[GetConsignments.getConsignments.Data](GetConsignments.getConsignments.document).result.map(result => result match {
//      case Right(r) => Ok(views.html.showCollections(r.data.getConsignments))
//      case Left(ex) => InternalServerError(ex.errors.toString())
//    }
//    )



  }


}
