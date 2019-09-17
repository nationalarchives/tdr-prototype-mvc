package graphql.tdr

import scala.concurrent.Future

trait TdrBackendClient {
  def send(body: String): Future[(Int, String)]
}
