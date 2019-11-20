package controllers

import akka.actor.ActorSystem
import auth.Authorisers.IsConsignmentCreator
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import graphql.GraphQLClientProvider
import graphql.codegen.CreateMultipleFiles.createMultipleFiles.{Data, Variables, document}
import graphql.codegen.types.CreateFileInput
import graphql.tdr.TdrSignRequestClient
import javax.inject._
import play.api.Configuration
import play.api.libs.json.{Json, _}
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UploadController @Inject()(
                                  client: GraphQLClientProvider,
                                  controllerComponents: ControllerComponents,
                                  system: ActorSystem,
                                  config: Configuration,
                                  isConsignmentCreator: IsConsignmentCreator
                                )(implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {


  private val accessKeyID: String = config.get[String]("aws.access.key.id")
  private val accessKeySecret: String = config.get[String]("aws.secret.access.key")
  private val environment: String = config.get[String]("app.environment")
  private val stateMachineArn: String = config.get[String]("statemachine.arn")

  case class Input(consignmentId: String)
  case class StateMachineInput(input: String, stateMachineArn: String)
  implicit val InputWrites = Json.writes[Input]
  implicit val StateMachineWrites = Json.writes[StateMachineInput]


  def index(consignmentId: Int, seriesId: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.upload(consignmentId, seriesId))
  }


  def saveFileData = Action.async(parse.json) { request =>
    val result = request.body.validate[FileInputs]
    result.fold(
      errors => {
        Future.successful(InternalServerError(errors.toString()))
      },
      fileInputs => {
        val graphQlClient = client.graphqlClient

        graphQlClient.query[Data, Variables](document, Variables(fileInputs.data)).result.map {
          case Right(r) =>
            val pathToId: Map[String, String] = r.data.createMultipleFiles map (f => f.path.toString -> f.id.toString) toMap
            val output: Output = Output(pathToId, s"tdr-upload-files-$environment")
            Ok(Json.toJson(output))
          case Left(ex) => InternalServerError(ex.errors.toString())
        }
      }
    )
  }

  def upload(consignmentId: Int) = Action { implicit request =>
    val client = new TdrSignRequestClient(config, "stepfunction.uri")
    val input = Input(consignmentId.toString)
    val inputString = Json.asciiStringify(Json.toJson(input))
    client.send(Json.asciiStringify(Json.toJson(StateMachineInput(inputString, stateMachineArn))))

    Redirect(routes.FileStatusController.getFileStatus(consignmentId))
  }

  case class FileInputs(data: List[CreateFileInput])

  case class Output(pathMap: Map[String, String], bucketName: String)

  implicit val createFileInputReads: Reads[CreateFileInput] = Json.reads[CreateFileInput]
  implicit val fileInputReads: Reads[FileInputs] = Json.reads[FileInputs]
    implicit val outputWrites: OWrites[Output] = Json.writes[Output]
}
