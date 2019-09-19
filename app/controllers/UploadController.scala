package controllers

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectRequest
import graphql.GraphQLClientProvider
import graphql.codegen.CreateMultipleFiles.createMultipleFiles.{Data, Variables, document}
import graphql.codegen.types.CreateFileInput
import javax.inject._
import play.api.Configuration
import play.api.libs.json.{Json, _}
import play.api.mvc._

import scala.collection.immutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

@Singleton
class UploadController @Inject()(
  client: GraphQLClientProvider,
  controllerComponents: ControllerComponents,
  config: Configuration
)(implicit val ex: ExecutionContext) extends AbstractController(controllerComponents) {

  private val accessKeyID : String =  config.get[String]("aws.access.key.id")
  private val accessKeySecret : String =  config.get[String]("aws.secret.access.key")

  def index(consignmentId: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.upload())
//    val codeOption = request.queryString.get("code").flatMap(_.headOption)
//
//    val cognitoConfig = configuration.underlying.as[CognitoUploadConfig]("cognito.upload")
//
//    // TODO: Add state parameter? How do we validate it?
//    val url = s"${cognitoConfig.authenticationUrl}?response_type=code&scope=${cognitoConfig.scopes}&client_id=${cognitoConfig.clientId}&redirect_uri=${cognitoConfig.redirectUrl}"
//
//    codeOption match {
//      case Some(code) => Ok(views.html.upload())
//      case None => Redirect(url)
//    }
  }
  def upload = Action(parse.multipartFormData) { request =>
    val a: String = request.body.dataParts("file-id-data").head
    //Convert this to an object to do the file mapping and make the s3 upload async
    request.body
      .files
      .map { file  =>
        val s3Client = AmazonS3ClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyID, accessKeySecret)))
          .withRegion("eu-west-2")
          .build()

        val request = new PutObjectRequest("tdr-files-test", file.filename, file.ref)
        s3Client.putObject(request)
      }
      Ok("File uploaded")

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
        val appSyncClient = client.graphqlClient(List())
        appSyncClient.query[Data, Variables](document,Variables(fileInputs.data)).result.map {
          case Right(r) =>
            Ok(Json.toJson(r.data.createMultipleFiles.map {
              f => Map("id" -> f.id.toString, "path" -> f.path.toString)
            }))
          case Left(ex) => InternalServerError(ex.errors.toString())
        }

      }
    )

  }
}

case class CognitoUploadConfig(
  authenticationUrl: String,
  redirectUrl: String,
  scopes: String,
  clientId: String)
