package auth

import java.net.URI

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.Inject
import play.api.Configuration
import providers.CognitoSocialProfile
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, PutItemRequest}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class DynamoUserService @Inject()(configuration: Configuration)(implicit ex: ExecutionContext) extends IdentityService[User] {

  private val userTable = "Users"

  private val dynamoDbEndpoint = new URI(configuration.get[String]("userdb.endpoint"))
  private val client = DynamoDbClient.builder.endpointOverride(dynamoDbEndpoint).build

  def save(profile: CognitoSocialProfile): Future[User] = {
    retrieve(profile.loginInfo).map(optionUser => {
      val user = optionUser.getOrElse(User(profile.loginInfo))
      save(user)
      user
    })
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val userIdAttributeValue = AttributeValue.builder.s(loginInfo.providerKey).build
    val itemRequest = GetItemRequest.builder.tableName(userTable).key(Map("id" -> userIdAttributeValue).asJava).build
    val response = client.getItem(itemRequest)

    val user = response.item match {
      case item if item.isEmpty => None
      case _ => Some(User(loginInfo))
    }

    Future.successful(user)
  }

  private def save(user: User): Unit = {
    val idValue = AttributeValue.builder.s(user.loginInfo.providerKey).build
    val attributes = Map("id" -> idValue).asJava
    val putItemRequest = PutItemRequest.builder.tableName(userTable).item(attributes).build
    client.putItem(putItemRequest)
  }
}
