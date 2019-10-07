package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.GoogleTotpInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import graphql.GraphQLClientProvider
import graphql.codegen.FindTotp.findTotp
import graphql.codegen.AddTotp.addTotp
import graphql.codegen.UpdateTotp.updateTotp
import graphql.codegen.DeleteTotp.removeTotp
import graphql.codegen.types
import graphql.tdr.TdrGraphQLClient
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class TotpDao @Inject()(client: GraphQLClientProvider)(implicit ec: ExecutionContext, implicit val classTag: ClassTag[GoogleTotpInfo])
  extends DelegableAuthInfoDAO[GoogleTotpInfo]  {

  val graphqlClient: TdrGraphQLClient = client.graphqlClient(List())

  override def find(loginInfo: LoginInfo): Future[Option[GoogleTotpInfo]] = {
    val vars = findTotp.Variables(loginInfo.providerKey)
    graphqlClient.query[findTotp.Data, findTotp.Variables](findTotp.document, vars).result.map {
      case Right(r) => r.data.findTotp.map(t => GoogleTotpInfo(t.sharedKey, t.scratchCodes.map(s => PasswordInfo(s.hasher, s.password, s.salt))))
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: GoogleTotpInfo): Future[GoogleTotpInfo] = {
    val totpInput = types.TotpInfoInput(loginInfo.providerKey, authInfo.sharedKey, authInfo.scratchCodes.map(s => types.TotpScratchCodesInput(s.hasher, s.password, s.salt)).toList)
    val vars = addTotp.Variables(totpInput)
    graphqlClient.query[addTotp.Data, addTotp.Variables](addTotp.document, vars).result.map {
      case Right(r) => r.data.addTotp.map(t => GoogleTotpInfo(t.sharedKey, t.scratchCodes.map(s => PasswordInfo(s.hasher, s.password, s.salt))))
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }
    Future.successful(GoogleTotpInfo("sharedKey", Seq(PasswordInfo("", "", Option.apply("")))))
  }

  override def update(loginInfo: LoginInfo, authInfo: GoogleTotpInfo): Future[GoogleTotpInfo] = {
    val totpInput = types.TotpInfoInput(loginInfo.providerKey, authInfo.sharedKey, authInfo.scratchCodes.map(s => types.TotpScratchCodesInput(s.hasher, s.password, s.salt)).toList)
    val vars = addTotp.Variables(totpInput)
    graphqlClient.query[addTotp.Data, addTotp.Variables](updateTotp.document, vars).result.map {
      case Right(r) => r.data.addTotp
      case Left(ex) => throw new RuntimeException(ex.errors.mkString)
    }
    Future.successful(GoogleTotpInfo(authInfo.sharedKey, authInfo.scratchCodes))
  }

  override def save(loginInfo: LoginInfo, authInfo: GoogleTotpInfo): Future[GoogleTotpInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    val vars = removeTotp.Variables(loginInfo.providerKey)
    graphqlClient.query[removeTotp.Data, removeTotp.Variables](removeTotp.document, vars).result
    Future.successful(())
  }
}
