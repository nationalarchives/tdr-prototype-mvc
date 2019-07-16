package graphQL

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.github.jarlakxen.drunk._

import scala.collection.immutable.Seq
import scala.concurrent.Future

object TDRGraphQLClient {

  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val uri: Uri = Uri(s"https://qad2wpgi3befniyihgl42yvfea.appsync-api.eu-west-2.amazonaws.com/graphql")

  val http: HttpExt = Http()
  val flow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = http.outgoingConnectionHttps(uri.authority.host.address(), uri.effectivePort)

  def appSyncClient (oathHeaders: Seq[HttpHeader]) = GraphQLClient(uri, flow, clientOptions = ClientOptions.Default, oathHeaders)
}
