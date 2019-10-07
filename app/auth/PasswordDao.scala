package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import graphql.GraphQLClientProvider
import graphql.codegen.AddPassword.addPassword
import graphql.codegen.FindPassword.findPassword
import graphql.codegen.RemovePassword.removePassword
import graphql.codegen.UpdatePassword.updatePassword
import graphql.codegen.types.PasswordInput
import graphql.tdr.TdrGraphQLClient
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class PasswordDao @Inject()(client: GraphQLClientProvider)(implicit ec: ExecutionContext, implicit val classTag: ClassTag[PasswordInfo])
  extends DelegableAuthInfoDAO[PasswordInfo] {

  val graphqlClient: TdrGraphQLClient = client.graphqlClient

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val vars: findPassword.Variables = findPassword.Variables(loginInfo.providerKey)
    graphqlClient.query[findPassword.Data, findPassword.Variables](findPassword.document, vars).result.map {
      case Right(r) => r.data.findPassword.map(p => PasswordInfo(p.hasher, p.password, p.salt))
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val passwordInput = PasswordInput(loginInfo.providerKey, authInfo.hasher, authInfo.password)
    val vars: addPassword.Variables = addPassword.Variables(passwordInput)

    graphqlClient.query[addPassword.Data, addPassword.Variables](addPassword.document, vars).result.map {
      case Right(r) => r.data.addPassword.map(p => PasswordInfo(p.hasher, p.password, p.salt)).get
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val passwordInput = PasswordInput(loginInfo.providerKey, authInfo.hasher, authInfo.password)
    val vars: updatePassword.Variables = updatePassword.Variables(passwordInput)
    graphqlClient.query[updatePassword.Data, updatePassword.Variables](updatePassword.document, vars).result.map {
      case Right(_) => PasswordInfo(authInfo.hasher, authInfo.password, authInfo.salt)
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }

  }
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    val vars = removePassword.Variables(loginInfo.providerKey)
    graphqlClient.query[removePassword.Data, removePassword.Variables](removePassword.document, vars).result.map {
      case Right(_) => Unit
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }
  }
}
