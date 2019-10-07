package auth

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import graphql.GraphQLClientProvider
import graphql.codegen.GetUser.getUser
import graphql.codegen.CreateUser.createUser
import graphql.codegen.types.UserInput
import graphql.codegen.CreatePasswordResetToken.createPasswordResetToken
import graphql.tdr.TdrGraphQLClient
import graphql.tdr.TdrGraphQLClient.GraphQLResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UserService @Inject()(client: GraphQLClientProvider,
                            passwordHasher: PasswordHasher,
                            authInfoRepository: AuthInfoRepository)
                           (implicit ec: ExecutionContext)
  extends IdentityService[User]
{


  val graphqlClient: TdrGraphQLClient = client.graphqlClient

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val vars = getUser.Variables(loginInfo.providerKey, loginInfo.providerID)
    graphqlClient.query[getUser.Data, getUser.Variables](getUser.document, vars).result map {
      case Right(r) =>
        r.data.getUser.map(u => User(u.id, u.firstName, u.lastName, u.email, u.providerId, u.providerKey))
      case Left(ex) => throw new RuntimeException(ex.errors.head)
    }
  }

  def create(data: SignUpForm.Data): Future[LoginInfo] = {
    val userInput: UserInput = UserInput(data.firstName, data.lastName, data.email, CredentialsProvider.ID)
    val vars = createUser.Variables(userInput)
    graphqlClient.query[createUser.Data, createUser.Variables](createUser.document, vars).result.andThen {
      case Failure(_) => None
      case Success(response: GraphQLResponse[createUser.Data]) =>
        response.map(r => {
          r.data.createUser.map(u => {
            val loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, u.email)
            val authInfo: PasswordInfo = passwordHasher.hash(data.password)
            authInfoRepository.add(loginInfo, authInfo)
          })
        })

    }.map(_ => LoginInfo(CredentialsProvider.ID, data.email))
  }

  def createOrUpdatePasswordResetToken(email: String) = {
    val vars = createPasswordResetToken.Variables(email)
    graphqlClient.query[createPasswordResetToken.Data, createPasswordResetToken.Variables](createPasswordResetToken.document, vars).result map {
      case Right(r) => r.data.createPasswordResetToken.map(t => t.token).get
      case Left(_) => throw new Exception
    }
  }

  def updatePassword(email: String, newPassword: String) = {
    val loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, email)
    val authInfo: PasswordInfo = passwordHasher.hash(newPassword)
    authInfoRepository.update(loginInfo, authInfo)
  }


}

