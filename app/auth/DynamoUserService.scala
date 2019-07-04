package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.Inject
import providers.CognitoSocialProfile
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, PutItemRequest}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class DynamoUserService @Inject()(dbClient: UserDbClient)(implicit ex: ExecutionContext) extends IdentityService[User] {

  private val userTable = "Users"

  private val client = dbClient.client

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
