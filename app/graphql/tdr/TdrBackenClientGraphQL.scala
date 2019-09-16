package graphql.tdr

import akka.actor.ActorSystem

import scala.collection.immutable.Seq
import scala.concurrent.Future
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.github.jarlakxen.drunk.backend.AkkaHttpBackend

class TdrBackendClientGraphQL(uri: Uri, oathHeaders: Seq[HttpHeader]) extends TdrBackendClient {

  implicit val as: ActorSystem = ActorSystem("GraphQLClient")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  val backend = AkkaHttpBackend(uri, oathHeaders)

  def send(body: String): Future[(Int, String)] = {
    backend.send(body)
  }
}
