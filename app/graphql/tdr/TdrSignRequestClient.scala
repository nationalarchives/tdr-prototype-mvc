package graphql.tdr

import java.io.ByteArrayInputStream
import akka.actor.ActorSystem
import play.api.Configuration
import akka.http.scaladsl.model.Uri
import ca.ryangreen.apigateway.generic.{GenericApiGatewayClientBuilder, GenericApiGatewayRequestBuilder, GenericApiGatewayResponse}
import com.amazonaws.ClientConfiguration
import com.amazonaws.http.HttpMethodName
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.auth.{BasicAWSCredentials,AWSStaticCredentialsProvider}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class TdrSignRequestClient(config:Configuration, uriPath: String) extends TdrBackendClient {

  implicit val as: ActorSystem = ActorSystem("GraphQLClient")
  implicit val ec: ExecutionContext = as.dispatcher

  private val uri: Uri = Uri(config.get[String](uriPath))
  private val accessKeyID : String =  config.get[String]("aws.access.key.id")
  private val accessKeySecret : String =  config.get[String]("aws.secret.access.key")


  val apiGatewayClient = new GenericApiGatewayClientBuilder()
    .withClientConfiguration(new ClientConfiguration)
    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyID, accessKeySecret)))
    .withEndpoint(uri.authority.toString())
    .withRegion(Region.getRegion(Regions.EU_WEST_2))
    .build


  override def send(body: String): Future[(Int, String)] = {
    val headers = Map("Content-Type" -> "application/json").asJava
    val request = new GenericApiGatewayRequestBuilder()
      .withBody(new ByteArrayInputStream(body.getBytes))
      .withHttpMethod(HttpMethodName.POST)
      .withHeaders(headers)
      .withResourcePath(uri.path.toString())
      .build
    val response: GenericApiGatewayResponse = apiGatewayClient.execute(request)
      Future(response.getHttpResponse.getStatusCode, response.getBody)
  }

}

