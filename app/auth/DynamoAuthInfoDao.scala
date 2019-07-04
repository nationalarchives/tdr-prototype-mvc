package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, PutItemRequest}

import scala.collection.JavaConverters._
import scala.concurrent.Future

class DynamoAuthInfoDao @Inject()(dbClient: UserDbClient) extends DelegableAuthInfoDAO[OAuth2Info] {

  private val tokenTable = "UserTokens"
  val client = dbClient.client

  override def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    val userIdAttributeValue = AttributeValue.builder.s(loginInfo.providerKey).build
    val itemRequest = GetItemRequest.builder.tableName(tokenTable).key(Map("id" -> userIdAttributeValue).asJava).build
    val response = client.getItem(itemRequest)

    val authInfo = response.item match {
      case item if item.isEmpty => None
      case item => {
        val properties = item.entrySet().asScala.map(entry => (entry.getKey, entry.getValue)).toMap
        val accessToken = properties("accessToken").s
        val refreshToken = properties.get("refreshToken").map(token => token.s)
        val idToken = properties.get("idToken").map(token => token.s)

        val extraParams = idToken.map(t => Map("idToken" -> t))

        Some(OAuth2Info(accessToken, refreshToken = refreshToken, params = extraParams))
      }
    }

    Future.successful(authInfo)
  }

  override def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = save(loginInfo, authInfo)

  override def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = save(loginInfo, authInfo)

  override def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val attributes = Map(
      "id" -> AttributeValue.builder.s(loginInfo.providerKey).build,
      "accessToken" -> AttributeValue.builder.s(authInfo.accessToken).build,
      "idToken" -> authInfo.params.get.get("idToken").map(token => AttributeValue.builder.s(authInfo.accessToken).build).getOrElse(AttributeValue.builder.nul(true).build),
      "refreshToken" -> authInfo.refreshToken.map(token => AttributeValue.builder.s(authInfo.accessToken).build).getOrElse(AttributeValue.builder.nul(true).build)
    ).asJava
    val putItemRequest = PutItemRequest.builder.tableName(tokenTable).item(attributes).build

    client.putItem(putItemRequest)

    Future.successful(authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}
