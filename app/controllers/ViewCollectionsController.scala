package controllers

import graphql.GraphQLClientProvider
import graphql.codegen.GetConsignments
import javax.inject.{Inject, _}
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ViewCollectionsController @Inject()(
                                           client: GraphQLClientProvider,
                                           controllerComponents: ControllerComponents,
                                           configuration: Configuration)(
                                           implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action.async { implicit request: Request[AnyContent] =>

    val appSyncClient = client.graphqlClient(List())

    //        appSyncClient.query[CreateConsignment.createCon.Data, CreateConsignment.createCon.Variables](CreateConsignment.createCon.document,
    //          CreateConsignment.createCon.Variables("drunkTest", 4)).result.map(result => result match {
    //          case Right(r) => println("Saved")
    //          case Left(ex) => println(ex)
    //
    //        })

    appSyncClient.query[GetConsignments.getConsignments.Data](GetConsignments.getConsignments.document).result.map(result => result match {
      case Right(r) => Ok(views.html.showCollections(r.data.getConsignments))
      case Left(ex) => InternalServerError(ex.errors.toString())
    })

  }


}
