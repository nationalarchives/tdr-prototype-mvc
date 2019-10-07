package graphql.tdr

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.github.jarlakxen.drunk.backend.AkkaHttpBackend

import scala.concurrent.Future

class TdrBackendClientGraphQL(uri: Uri) extends TdrBackendClient {

  implicit val as: ActorSystem = ActorSystem("GraphQLClient")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  val backend = AkkaHttpBackend(uri)

  def send(body: String): Future[(Int, String)] = {
    backend.send(body)
  }
}
