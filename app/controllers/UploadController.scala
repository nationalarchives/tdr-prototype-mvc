package controllers

import java.io.ByteArrayInputStream
import java.nio.file.Files

import actors.S3UploadActor
import actors.S3UploadActor.S3Request
import akka.actor.ActorSystem
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import graphql.GraphQLClientProvider
import graphql.codegen.CreateMultipleFiles.createMultipleFiles.{Data, Variables, document}
import graphql.codegen.types.CreateFileInput
import io.circe.parser.decode
import javax.inject._
import play.api.Configuration
import play.api.libs.json.{Json, _}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UploadController @Inject()(
                                  client: GraphQLClientProvider,
                                  controllerComponents: ControllerComponents,
                                  system: ActorSystem,
                                  config: Configuration,
                                )(implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {


  private val accessKeyID: String = config.get[String]("aws.access.key.id")
  private val accessKeySecret: String = config.get[String]("aws.secret.access.key")

  private val s3Actor = system.actorOf(S3UploadActor.props, "s3-upload-actor")

  def index(consignmentId: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.upload(consignmentId))
  }

  def upload(consignmentId: Int) = Action(parse.multipartFormData) { request =>
    val fileIdString: String = request.body.dataParts("file-id-data").head

    val fileIdMap: Map[String, String] = decode[Map[String,String]](fileIdString) match {
      case Right(r) => r
      case Left(_) => Map()
    }

    request.body
      .files
      .foreach { file =>
        fileIdMap.get(file.filename) match {
          case Some(id) =>
            val fileArr: Array[Byte] = Files.readAllBytes(file.ref.path)
            val metaData: ObjectMetadata = new ObjectMetadata()
            metaData.setContentLength(fileArr.length)
            val request = new PutObjectRequest("tdr-files-test", s"$consignmentId/$id", new ByteArrayInputStream(fileArr), metaData)
            s3Actor ! S3Request(request, new BasicAWSCredentials(accessKeyID, accessKeySecret))
          case None =>
        }
      }
    Redirect(routes.FileStatusController.getFileStatus(consignmentId))

  }

  case class FileInputs(data: List[CreateFileInput])

  implicit val createFileInputReads: Reads[CreateFileInput] = Json.reads[CreateFileInput]
  implicit val fileInputReads: Reads[FileInputs] = Json.reads[FileInputs]


  def saveFileData = Action.async(parse.json) { request =>
    val result = request.body.validate[FileInputs]
    result.fold(
      errors => {
        Future.apply(InternalServerError(errors.toString()))
      },
      fileInputs => {
        val graphQLClient = client.graphqlClient(List())
        graphQLClient.query[Data, Variables](document, Variables(fileInputs.data)).result.map {
          case Right(r) =>
            Ok(Json.toJson(r.data.createMultipleFiles map (f => f.path.toString -> f.id.toString) toMap))
          case Left(ex) => InternalServerError(ex.errors.toString())
        }
      }
    )

  }
}
