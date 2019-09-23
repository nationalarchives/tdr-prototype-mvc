package actors

import akka.actor._
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectRequest

object S3UploadActor {
  def props = Props[S3UploadActor]

  case class S3Request(request: PutObjectRequest, credentials: BasicAWSCredentials)
}

class S3UploadActor extends Actor {

  import S3UploadActor._

  def receive = {
    case S3Request(request: PutObjectRequest, credentials: BasicAWSCredentials) =>
      val s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion("eu-west-2")
        .build()

      s3Client.putObject(request)
  }
}