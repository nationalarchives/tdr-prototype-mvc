package controllers

import akka.http.scaladsl.model.headers.RawHeader
import graphQL.TDRGraphQLClient
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import javax.inject.{Inject, Singleton}
import model.TdrCollection
import modules.TDRAttributes
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import sangria.macros._

import scala.concurrent.ExecutionContext

@Singleton
class CreateCollectionController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration)(
  implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action.async { implicit request: Request[AnyContent] =>

    case class CreateCollectionMutation(createdCollection: TdrCollection)
    case class CreateCollectionResult(createCollection: TdrCollection)

    implicit val tdrCollectionDecoder: Decoder[TdrCollection] = deriveDecoder
    implicit val createCollectionMutationDecoder: Decoder[CreateCollectionMutation] = deriveDecoder
    implicit val createCollectionResultDecoder: Decoder[CreateCollectionResult] = deriveDecoder

    case class CreatedCollection(id: String)

    implicit val createdCollectionDecoder: Decoder[CreatedCollection] = deriveDecoder

    val accessToken = request.attrs.get(TDRAttributes.OAuthAccessTokenKey).get.accessToken
    val header = RawHeader("Authorization", accessToken)

    val appSyncClient = TDRGraphQLClient.appSyncClient(List(header))

    val createCollectionsDoc =
      graphql"""
           mutation {
               createCollection(name: "PlayMVCTest1", copyright: "copyright", closure: "closure", legalStatus: "legalStatus") {
                  id
                  name
                  copyright
                  closure
                  legalStatus
                }
           }
           """

    appSyncClient.query[CreateCollectionResult](createCollectionsDoc).result.map(result => result match {
      case Right(r) => Ok(views.html.createCollection())
      case Left(ex) => InternalServerError(ex.errors.toString())})
  }
}
