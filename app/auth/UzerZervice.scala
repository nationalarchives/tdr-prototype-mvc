package auth

import akka.actor.ActorSystem
import forms.SignUpForm
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.cognitoidp.model.{AdminCreateUserRequest, AdminCreateUserResult, AttributeType}
import com.amazonaws.services.cognitoidp.{AWSCognitoIdentityProvider, AWSCognitoIdentityProviderClientBuilder}
import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.{ExecutionContext, Future}


class UzerZervice {

  implicit val as: ActorSystem = ActorSystem("GraphQLClient")
  implicit val ec: ExecutionContext = as.dispatcher

  //config from
  val cognitoID: String = "439a68k19tugh89sevo7eh9pcc"
  val cognitoKey: String = "ade60gaujm6oaee9s9iib3rnksmq5fcd6i1pk607q3drdidrkdd"
  val poolId: String = "eu-west-2_6Mn0M2i9C"
  val region: Regions = Regions.EU_WEST_2


  //names for things in congnito db...
  val LOCATION = "custom:location"
  val LASTNAME = "custom:lastname"

  val EMAIL = "email"

  val mIdentityProvider: AWSCognitoIdentityProvider = {
    val credentials: BasicAWSCredentials = new BasicAWSCredentials(cognitoID, cognitoKey)
    val credProvider: AWSStaticCredentialsProvider = new AWSStaticCredentialsProvider(credentials)
    AWSCognitoIdentityProviderClientBuilder.standard.withCredentials(credProvider).withRegion(region).build
  }

  def createNewUser(userInfo: SignUpForm.Data) = {
    val emailAddr: String = userInfo.email  //it's checked for emailness and non empty in Form
    findUserByEmailAddr(userInfo.email) match {
      case Some(_) => throw new Exception("duplicate email: caller should check not already existing")
      case None =>
        val cognitoReequest: AdminCreateUserRequest = new AdminCreateUserRequest()
            .withUserPoolId(poolId)
            .withUsername(userInfo.firstName) //TODO = we have firstname
            .withUserAttributes(
              new AttributeType().withName(EMAIL).withValue(emailAddr),
              new AttributeType().withName(LASTNAME).withValue(userInfo.lastName),
              new AttributeType().withName("email_verified").withValue("true")
            )
        val xx: AdminCreateUserResult = mIdentityProvider.adminCreateUser(cognitoReequest)
        Future(LoginInfo("providerId", "providerKey"))
    }
  }

  def findUserByEmailAddr(str: String): Option[SignUpForm.Data] = None


}
