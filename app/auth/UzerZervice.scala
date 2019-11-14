package auth

import forms.SignUpForm
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.cognitoidp.model.{AdminCreateUserRequest, AdminCreateUserResult, AdminInitiateAuthRequest, AdminInitiateAuthResult, AdminRespondToAuthChallengeRequest, AttributeType, AuthFlowType}
import com.amazonaws.services.cognitoidp.{AWSCognitoIdentityProvider, AWSCognitoIdentityProviderClientBuilder}
import forms.LoginForm.LoginData

import scala.collection.JavaConverters._

class UzerZervice{

  //config from
  val cognitoID: String = "AKIATTD47MKYZZC6CJ6W"
  val cognitoKey: String = "Kw3440AMWqGED/aVWyXCwmH/0rBzvUhA3OI4Pipl"
  val clientID: String = "5pekokle5lubuqokujaffk076f"  //"Your Cognito client ID goes here"
  val poolId: String = "eu-west-2_MV1Sze5fS"
  val region: Regions = Regions.EU_WEST_2

  val tempPwd = "Pa$$w0rd"

  //names for things in congnito db...
  val LASTNAME = "custom:location" //TODO: fix naming in cognito
  val EMAIL = "email"

  val mIdentityProvider: AWSCognitoIdentityProvider = {
    val credentials: BasicAWSCredentials = new BasicAWSCredentials(cognitoID, cognitoKey)
    val credProvider: AWSStaticCredentialsProvider = new AWSStaticCredentialsProvider(credentials)
    AWSCognitoIdentityProviderClientBuilder.standard.withCredentials(credProvider).withRegion(region).build
  }

  def createNewUser(userInfo: SignUpForm.Data): Unit = {
    val emailAddr: String = userInfo.email  //it's checked for emailness and non empty in Form
    findUserByEmailAddr(userInfo.email) match {
      case Some(_) => throw new Exception("duplicate email: caller should check not already existing")
      case None =>
        val cognitoRequest: AdminCreateUserRequest = new AdminCreateUserRequest()
            .withUserPoolId(poolId)
            .withTemporaryPassword(tempPwd)
            .withUsername(userInfo.firstName) //TODO = we have firstname
            .withUserAttributes(
              new AttributeType().withName(EMAIL).withValue(emailAddr),
              new AttributeType().withName(LASTNAME).withValue(userInfo.lastName),
              new AttributeType().withName("email_verified").withValue("true")
            )
        val createdNewUserWithTempPasswordResult: AdminCreateUserResult = mIdentityProvider.adminCreateUser(cognitoRequest)
        changePasswordForNewUser(userInfo.firstName, userInfo.password)
    }
  }

  private def changePasswordForNewUser(userName: String, newPassword: String): Unit = {
    //get a session id (with a NEW_PASSWORD_REQUEST challenge)
    val authParams = Map("USERNAME" -> userName, "PASSWORD" -> tempPwd).asJava
    val authRequest = new AdminInitiateAuthRequest()
      .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
      .withUserPoolId(poolId)
      .withClientId(clientID)
      .withAuthParameters(authParams)
    val authResult: AdminInitiateAuthResult = mIdentityProvider.adminInitiateAuth(authRequest)
    val session = authResult.getSession
    //with the session id we can change the password
    val challengeResponseParams = Map("USERNAME" -> userName, "NEW_PASSWORD" -> newPassword).asJava
    val request = new AdminRespondToAuthChallengeRequest()
        .withUserPoolId(poolId)
        .withClientId(clientID)
        .withChallengeName("NEW_PASSWORD_REQUIRED")
        .withChallengeResponses(challengeResponseParams)
        .withSession(session)
    val okResult = mIdentityProvider.adminRespondToAuthChallenge(request)
  }

  def findUserByEmailAddr(str: String): Option[SignUpForm.Data] = None

  def retrieve(loginData: LoginData): SessionInfo = {
    sessionLogin("magic8", loginData.password)
  }

  def sessionLogin(userName: String, password: String): SessionInfo = {
    val authParams = Map("USERNAME" -> userName, "PASSWORD" -> password).asJava
    val authRequest = new AdminInitiateAuthRequest()
      .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
      .withUserPoolId(poolId)
      .withClientId(clientID)
      .withAuthParameters(authParams)
    val authResult: AdminInitiateAuthResult = mIdentityProvider.adminInitiateAuth(authRequest)
    val session = authResult.getSession
    val idToken = authResult.getAuthenticationResult.getIdToken
    val challengeResult = authResult.getChallengeName
    SessionInfo(session, idToken, challengeResult)
  }

  case class SessionInfo(session: String, idToken: String, challengeResult: String)

}
