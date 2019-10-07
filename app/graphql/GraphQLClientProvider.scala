package graphql

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model._
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import graphql.tdr._
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.Future

class GraphQLClientProvider @Inject()(
                                  configuration: Configuration) {

  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val uri: Uri = Uri(configuration.get[String]("graphql.uri"))
  val http: HttpExt = Http()
  val flow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] =
    if (uri.scheme == "http") {
      http.outgoingConnection(uri.authority.host.address(), uri.effectivePort)
    } else {
      http.outgoingConnectionHttps(uri.authority.host.address(), uri.effectivePort)
    }

  def graphqlClient: TdrGraphQLClient = {
    if (uri.scheme == "http") {
      TdrGraphQLClient(uri)
    }
    else {
      TdrGraphQLClient(configuration)
    }
  }
}




