package scala.steps

import graphql.codegen.GetConsignment.getConsignment
import graphql.codegen.GetConsignment.getConsignment.GetConsignment
import graphql.tdr.TdrGraphQLClient
import graphql.tdr.TdrGraphQLClient.GraphQLResponse
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object DatabaseResponseValidator extends FunSpec with Matchers {

  def runAndUnpackQuery(query: String, variables: Int, graphQlClient: TdrGraphQLClient)(implicit ex: ExecutionContext): GetConsignment = {
    query match {
      case "getConsignment" =>
        val consignmentDetails: Future[GraphQLResponse[getConsignment.Data]] =
        graphQlClient.query[getConsignment.Data, getConsignment.Variables](getConsignment.document, getConsignment.Variables(variables)).result
        unpackResponse(consignmentDetails)
      case other =>
        throw new NotImplementedError(s"No DatabaseResponseValidator query for $other")
    }
  }

  private def unpackResponse(responseF: Future[GraphQLResponse[getConsignment.Data]])(implicit ex: ExecutionContext): GetConsignment = {
    val getConsignmentF = for {
      consignmentDetails <- responseF
    } yield {
      consignmentDetails.right.get.data.getConsignment.get
    }
    Await.result(getConsignmentF, Duration.Inf)
  }

  def assertOnFields(expectedData: List[DatabaseResponseData], actual: GetConsignment): Unit = {
    expectedData.foreach { expected => expected.key match {
        case "name"             => expected.value shouldBe actual.name
        case "transferringBody" => expected.value shouldBe actual.transferringBody
        case other              => throw new NotImplementedError(s"No DatabaseResponseValidator assertion for key $other")
      }
    }
  }
}
