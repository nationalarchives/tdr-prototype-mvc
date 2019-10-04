package auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import graphql.GraphQLClientProvider
import graphql.codegen.GetConsignmentForCreator.getConsignmentForCreator
import javax.inject.Inject
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

object Authorisers {

  case class IsConsignmentCreator @Inject ()(client: GraphQLClientProvider, implicit val ec: ExecutionContext) extends Authorization[User, CookieAuthenticator] {
    override def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(implicit request: Request[B]): Future[Boolean] = {
      val graphqlClient = client.graphqlClient(List())
      val vars = getConsignmentForCreator.Variables(request.getQueryString("consignmentId").get.toInt, user.email)
      graphqlClient.query[getConsignmentForCreator.Data, getConsignmentForCreator.Variables](getConsignmentForCreator.document, vars).result.map {
        case Right(r) => r.data.getConsignmentForCreator.isDefined
        case Left(_) => false
      }
    }
  }
}
