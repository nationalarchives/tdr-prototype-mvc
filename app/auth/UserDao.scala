package auth

import java.net.URI

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, GetItemResponse, PutItemRequest}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import com.github.t3hnar.bcrypt._
import javax.inject.Inject


class UserDao @Inject()()(implicit ex: ExecutionContext) {
  private val dynamoDbEndpoint = new URI("https://dynamodb.eu-west-2.amazonaws.com")
  val client = DynamoDbClient.builder.endpointOverride(dynamoDbEndpoint).build

  def verifyUser(user: User)= {
    val usernameAttributeValue = AttributeValue.builder.s(user.username).build
    var verified = false
    val itemRequest = GetItemRequest.builder.tableName("play-users")
      .key(Map("username" -> usernameAttributeValue).asJava).build
    val response: GetItemResponse = client.getItem(itemRequest)

    if(!response.item().isEmpty) {
      val passwordFromDb = response.item.get("password").s()
      verified = user.password.isBcryptedSafe(passwordFromDb).getOrElse(false)
    }
    verified

  }

  implicit def stringToAttribute(value: String) = {
    AttributeValue.builder.s(value).build()
  }

  def createUser(createUser: CreateUser) = {
    val item: Map[String, AttributeValue] = Map("username" -> createUser.username, "password" -> createUser.password.bcrypt, "name" -> createUser.name)
    val putItemRequest = PutItemRequest.builder().tableName("play-users").item(mapAsJavaMap(item)).build()
    client.putItem(putItemRequest)
  }
}
