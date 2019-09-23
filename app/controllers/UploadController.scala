package controllers

import java.util.Date

import akka.actor.ActorSystem
import com.amazonaws.HttpMethod
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import graphql.GraphQLClientProvider
import graphql.codegen.CreateMultipleFiles.createMultipleFiles.{Data, Variables, document}
import graphql.codegen.types.CreateFileInput
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

  def index(consignmentId: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.upload(consignmentId))
  }

  private def generatePresignedUrl(key: String) = {
    val s3Client = AmazonS3ClientBuilder.standard
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyID, accessKeySecret)))
      .withRegion(Regions.EU_WEST_2).build

    // Set the pre-signed URL to expire after one hour.
    val expiration = new Date()
    var expTimeMillis = expiration.getTime
    expTimeMillis += 1000 * 60 * 60
    expiration.setTime(expTimeMillis)

    val generatePresignedUrlRequest = new GeneratePresignedUrlRequest("tdr-files-test", key).withMethod(HttpMethod.PUT).withExpiration(expiration)
    s3Client.generatePresignedUrl(generatePresignedUrlRequest).toString
  }

  case class FileInputs(data: List[CreateFileInput])

  implicit val createFileInputReads: Reads[CreateFileInput] = Json.reads[CreateFileInput]
  implicit val fileInputReads: Reads[FileInputs] = Json.reads[FileInputs]


  def getPresignedUrls = Action.async(parse.json) { request =>
    val result = request.body.validate[FileInputs]
    result.fold(
      errors => {
        Future.apply(InternalServerError(errors.toString()))
      },
      fileInputs => {
        val appSyncClient = client.graphqlClient(List())
        appSyncClient.query[Data, Variables](document, Variables(fileInputs.data)).result.map {
          case Right(r) =>
            Ok(Json.toJson(r.data.createMultipleFiles map (f => f.path.toString -> generatePresignedUrl(s"${f.consignmentId}/${f.id}")) toMap))
          case Left(ex) => InternalServerError(ex.errors.toString())
        }

      }
    )

  }
}
