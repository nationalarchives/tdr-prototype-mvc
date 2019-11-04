package auth

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import javax.inject.Inject
import play.api.Configuration
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, PutItemRequest}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class DynamoUserService @Inject()(
                                   dbClient: UserDbClient,
                                   configuration: Configuration
                                 ) (
                                   implicit ex: ExecutionContext
                                 ) extends IdentityService[Auth0User] {

  private val userTable = configuration.get[String]("userdb.tables.users")

  private val client = dbClient.client

  def save(profile: CommonSocialProfile): Future[Auth0User] = {
    retrieve(profile.loginInfo).map(existingUser => {
      val user = existingUser.getOrElse(Auth0User(profile.loginInfo.providerID, profile.loginInfo.providerKey))
      save(user)
      user
    })
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[Auth0User]] = {
    val userIdAttributeValue = AttributeValue.builder.s(loginInfo.providerKey).build
    val itemRequest = GetItemRequest.builder.tableName(userTable).key(Map("id" -> userIdAttributeValue).asJava).build
    val response = client.getItem(itemRequest)

    val user = response.item match {
      case item if item.isEmpty => None
      case _ => Some(Auth0User(loginInfo.providerID, loginInfo.providerKey))
    }

    Future.successful(user)
  }

  private def save(user: Auth0User): Unit = {
    val idValue = AttributeValue.builder.s(user.providerKey).build
    val attributes = Map("id" -> idValue).asJava
    val putItemRequest = PutItemRequest.builder.tableName(userTable).item(attributes).build
    client.putItem(putItemRequest)
  }
}

case class Auth0User(providerId: String, providerKey: String) extends Identity
