package controllers

import java.nio.file.Paths

import javax.inject._
import play.api.Configuration
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._


@Singleton
class UploadController @Inject()(
  controllerComponents: ControllerComponents,
  configuration: Configuration
) extends AbstractController(controllerComponents) {

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
    request.body
      .files
      .map { picture =>
        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
        val filename    = Paths.get(picture.filename).getFileName
        val fileSize    = picture.fileSize
        val contentType = picture.contentType

//        picture.ref.copyTo(Paths.get(s"/tmp/picture/$filename"), replace = true)
        Ok("File uploaded")
      }
      Ok("File uploaded")

  }


  case class FileData(data: List[FileInfo])
  case class FileInfo(consignmentId: Int, path: String, fileSize: Int, lastModifiedDate: String, clientSideChecksum: String, fileName: String)
  implicit val fileInfoReads: Reads[FileInfo] = Json.reads[FileInfo]
  implicit val fileDataReads: Reads[FileData] = Json.reads[FileData]



  def saveFileData = Action(parse.json) { request =>

//    implicit val tdrCollectionDecoder: Decoder[FileInfo] = deriveDecoder
    val result = request.body.validate[FileData]
    result.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))
      },
      fileInfo => {
        Ok(Json.obj("status" -> "OK", "message" -> ("FileInfo" + fileInfo.data + "' saved.")))
      }
    )
    Ok

  }

}

case class CognitoUploadConfig(
  authenticationUrl: String,
  redirectUrl: String,
  scopes: String,
  clientId: String)
