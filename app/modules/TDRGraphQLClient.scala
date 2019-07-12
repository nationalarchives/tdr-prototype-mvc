package modules

import com.github.jarlakxen.drunk._

class TDRGraphQLClient {

  val client = GraphQLClient(s"https://api/graphql")

  //val uri: Uri = Uri(s"https:api/graphql")

  //val http: HttpExt = Http()
  //val flow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = http.outgoingConnectionHttps(uri.authority.host.address(), uri.effectivePort)
  //val client = GraphQLClient(uri, flow, clientOptions = ClientOptions.Default, headers = Nil)
}
