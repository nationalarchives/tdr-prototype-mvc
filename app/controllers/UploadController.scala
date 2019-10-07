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
                                  silhouette: Silhouette[DefaultEnv],
                                  isConsignmentCreator: IsConsignmentCreator
                                )(implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {


  private val accessKeyID: String = config.get[String]("aws.access.key.id")
  private val accessKeySecret: String = config.get[String]("aws.secret.access.key")
  private val environment: String = config.get[String]("app.environment")


  def index(consignmentId: Int) = silhouette.SecuredAction(isConsignmentCreator) { implicit request: Request[AnyContent] =>
    Ok(views.html.upload(consignmentId))
  }


  def saveFileData = silhouette.SecuredAction(isConsignmentCreator).async(parse.json) { request =>
    val result = request.body.validate[FileInputs]
    result.fold(
      errors => {
        Future.apply(InternalServerError(errors.toString()))
      },
      fileInputs => {
        val appSyncClient = client.graphqlClient

        appSyncClient.query[Data, Variables](document, Variables(fileInputs.data)).result.map {
          case Right(r) =>
            val pathToId: Map[String, String] = r.data.createMultipleFiles map (f => f.path.toString -> f.id.toString) toMap
            val output: Output = Output(pathToId, getTemporaryCredentials, s"tdr-upload-files-$environment")
            Ok(Json.toJson(output))
          case Left(ex) => InternalServerError(ex.errors.toString())
        }
      }
    )
  }

  def upload(consignmentId: Int) = silhouette.SecuredAction(isConsignmentCreator) { implicit request =>
    Redirect(routes.FileStatusController.getFileStatus(consignmentId))
  }

  def getTemporaryCredentials: TemporaryCredentials = {
    // This would use an iam user which only has access to the upload bucket
    val credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyID, accessKeySecret))
    val tokenClient =  AWSSecurityTokenServiceClientBuilder.standard()
      .withCredentials(credentialsProvider)
      .build()


    val sessionTokenRequest = new GetSessionTokenRequest()
    sessionTokenRequest.setDurationSeconds(7200)
    val getSessionResult = tokenClient.getSessionToken(sessionTokenRequest)
    val credentials = getSessionResult.getCredentials
    TemporaryCredentials(credentials.getAccessKeyId, credentials.getSecretAccessKey, credentials.getSessionToken)
  }


  case class TemporaryCredentials(accessKeyId: String, secretAccessKey: String, sessionToken: String)

  case class FileInputs(data: List[CreateFileInput])

  case class Output(pathMap: Map[String, String], credentials: TemporaryCredentials, bucketName: String)

  implicit val createFileInputReads: Reads[CreateFileInput] = Json.reads[CreateFileInput]
  implicit val fileInputReads: Reads[FileInputs] = Json.reads[FileInputs]
  implicit val temporaryCredentialsWrites: OWrites[TemporaryCredentials] = Json.writes[TemporaryCredentials]
  implicit val outputWrites: OWrites[Output] = Json.writes[Output]
}
