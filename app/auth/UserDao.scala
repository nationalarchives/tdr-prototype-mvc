package auth

import java.net.URI

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, GetItemResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import com.github.t3hnar.bcrypt._
import javax.inject.Inject


class UserDao @Inject()()(implicit ex: ExecutionContext) {
  private val dynamoDbEndpoint = new URI("https://dynamodb.eu-west-2.amazonaws.com")
  val client = DynamoDbClient.builder.endpointOverride(dynamoDbEndpoint).build

  def verifyUser(username: String, password: String)= {
    val usernameAttributeValue = AttributeValue.builder.s(username).build
    val itemRequest = GetItemRequest.builder.tableName("play-users")
      .key(Map("username" -> usernameAttributeValue).asJava).build
    val response: GetItemResponse = client.getItem(itemRequest)

    if(!response.item().isEmpty) {
      val passwordFromDb = response.item.get("password").s()
      Future.fromTry(password.isBcryptedSafe(passwordFromDb))
    } else {
      Future.apply(false)
    }
  }
}
