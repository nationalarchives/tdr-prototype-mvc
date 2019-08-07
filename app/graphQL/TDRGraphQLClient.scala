package graphQL

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.github.jarlakxen.drunk._
import play.api.Configuration
import javax.inject.Inject

import scala.collection.immutable.Seq
import scala.concurrent.Future

class TDRGraphQLClient @Inject()(
  configuration: Configuration) {

  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  
  private val uri: Uri = Uri(configuration.get[String]("graphql.uri"))

  val http: HttpExt = Http()
  val flow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = http.outgoingConnectionHttps(uri.authority.host.address(), uri.effectivePort)

  def appSyncClient (oathHeaders: Seq[HttpHeader]) = GraphQLClient(uri, flow, clientOptions = ClientOptions.Default, oathHeaders)
}
