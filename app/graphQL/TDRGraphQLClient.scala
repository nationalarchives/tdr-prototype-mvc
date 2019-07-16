package graphQL

import javax.inject.Inject
import com.github.jarlakxen.drunk._
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sangria.macros._
import model.TdrCollection

class TDRGraphQLClient {

  case class GetCollectionsQuery(tdrCollection: TdrCollection)

  implicit val getCollectionsQueryDecoder: Decoder[GetCollectionsQuery] = deriveDecoder
  implicit val tdrCollectionDecoder: Decoder[TdrCollection] = deriveDecoder

  val apiUrl = "https://qad2wpgi3befniyihgl42yvfea.appsync-api.eu-west-2.amazonaws.com/graphql"
  val client = GraphQLClient(apiUrl)

  val getCollectionsDoc =
    graphql"""
      query GetCollections {
        getCollections {
          collections {
            id, name, legalStatus, closure, copyright
          }
        }
      }
    """
}
