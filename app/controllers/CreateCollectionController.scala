package controllers

import graphql.GraphQLClientProvider
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import javax.inject.{Inject, Singleton}
import model.{CreateCollectionData, TdrCollection}
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.data._
import play.api.data.Forms._
import sangria.macros._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

@Singleton
class CreateCollectionController @Inject()(
                                            client: GraphQLClientProvider,
                                            controllerComponents: ControllerComponents,
                                            configuration: Configuration)(
  implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {

  val userForm = Form(
    mapping(
      "consignmentName" -> text
    )(CreateCollectionData.apply)(CreateCollectionData.unapply)
  )

  def index(seriesId: Int) = Action { implicit request: Request[AnyContent] =>

    Ok(views.html.createCollection(userForm, seriesId))
  }

  def submit() = Action.async { implicit request: Request[AnyContent] =>

    val collectionData = userForm.bindFromRequest.get
    var userDefinedCollectionName = collectionData.collectionName

    case class Variables(collectionName: String)

    case class CreateCollectionMutation(createdCollection: TdrCollection)
    case class CreateCollectionResult(createCollection: TdrCollection)

    implicit val tdrCollectionDecoder: Decoder[TdrCollection] = deriveDecoder
    implicit val createCollectionMutationDecoder: Decoder[CreateCollectionMutation] = deriveDecoder
    implicit val createCollectionResultDecoder: Decoder[CreateCollectionResult] = deriveDecoder

    case class CreatedCollection(id: String)

    implicit val createdCollectionDecoder: Decoder[CreatedCollection] = deriveDecoder

    val appSyncClient = client.graphqlClient(List())

    val createCollectionsDoc =
      gql"""
           mutation($$collectionName: String!) {
               createCollection(name: $$collectionName, copyright: "copyright", closure: "closure", legalStatus: "legalStatus") {
                  id
                  name
                  copyright
                  closure
                  legalStatus
                }
           }
           """

    val variables = Variables(userDefinedCollectionName)

    appSyncClient.query[CreateCollectionResult, Variables](createCollectionsDoc, variables).result.map(result => result match {
      case Right(r) => Redirect(routes.UploadController.index(1))
      case Left(ex) => InternalServerError(ex.errors.toString())})
  }
}
