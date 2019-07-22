package controllers

import akka.http.scaladsl.model.headers.RawHeader
import graphQL.TDRGraphQLClient
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import javax.inject.{Inject, _}
import model.TdrCollection
import modules.TDRAttributes
import play.api.Configuration
import play.api.mvc._
import sangria.macros._

import scala.concurrent.ExecutionContext

@Singleton
class ViewCollectionsController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration)(
  implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action.async { implicit request: Request[AnyContent] =>
    case class GetCollectionsQuery(collections: Seq[TdrCollection])
    case class CollectionsResult(getCollections: GetCollectionsQuery)

    implicit val tdrCollectionDecoder: Decoder[TdrCollection] = deriveDecoder
    implicit val getCollectionsQueryDecoder: Decoder[GetCollectionsQuery] = deriveDecoder
    implicit val getCollectionsResultDecoder: Decoder[CollectionsResult] = deriveDecoder

    val accessToken = request.attrs.get(TDRAttributes.OAuthAccessTokenKey).get.accessToken
    val header = RawHeader("Authorization", accessToken)

    val appSyncClient = TDRGraphQLClient.appSyncClient(List(header))

    val getCollectionsDoc =
      graphql"""
           query MyQuery {
               getCollections {
                    collections {
                      id, name, legalStatus, closure, copyright
                   }
                }
           }
           """

    appSyncClient.query[CollectionsResult](getCollectionsDoc).result.map(result => result match {
      case Right(r) => Ok(views.html.showCollections(r.data.getCollections.collections))
      case Left(ex) => InternalServerError(ex.errors.toString())
    })
  }
}
