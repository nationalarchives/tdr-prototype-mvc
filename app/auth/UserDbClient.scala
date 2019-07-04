package auth

import java.net.URI

import javax.inject.Inject
import play.api.Configuration
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class UserDbClient @Inject()(configuration: Configuration) {
  private val dynamoDbEndpoint = new URI(configuration.get[String]("userdb.endpoint"))
  val client = DynamoDbClient.builder.endpointOverride(dynamoDbEndpoint).build
}
