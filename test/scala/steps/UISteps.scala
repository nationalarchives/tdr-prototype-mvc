package scala.steps


import java.net.URL

import cucumber.api.scala.{EN, ScalaDsl}
import graphql.GraphQLClientProvider

import DatabaseResponseValidator._
import io.cucumber.datatable.DataTable
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.{By, Keys, WebDriver}
import org.scalatest.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

class UISteps  extends ScalaDsl with EN with Matchers{

  var webDriver: WebDriver = _
  val defaultURL = "http://localhost:9000"

  val application: Application = GuiceApplicationBuilder().build()
  val graphQlClientProvider = application.injector.instanceOf[GraphQLClientProvider]
  implicit val executionContext: ExecutionContext = application.injector.instanceOf[ExecutionContext]
  val graphQlClient = graphQlClientProvider.graphqlClient

  Before() { scenario =>
      webDriver = UIUtility.getWebDriver
      //TODO: create test login & test series & also delete in a Before and an After
            //TODO: a single test series with a cascade delete is sufficient? or we need a whole keep track of what added and clean up?
  }

  After() { scenario =>
    webDriver.quit()
  }

  When("^I visit url (.*)$") {
    (url: String) =>
      webDriver.get(s"$defaultURL$url")
  }

  When("^I populate the view fields:$") {
    (dataTable: DataTable) =>
      val selectorData: List[FieldInsertData] = dataTable.asList(classOf[FieldInsertData]).asScala.toList
      selectorData.foreach(selectorData => {
        selectorData.fieldType match {
          case "input" =>
            val element = webDriver.findElement(By.cssSelector(selectorData.selector))
            if (element.getAttribute("value") == "")
              element.sendKeys(selectorData.value)
            else {
              element.sendKeys(Keys.chord(Keys.CONTROL, "a"))
              element.sendKeys(selectorData.value)
            }
          case "dropdown" =>
            val element = webDriver.findElement(By.cssSelector(selectorData.selector))
            val dropDown = new Select(element)
            dropDown.selectByVisibleText(selectorData.value)
          case "fileupload" =>
            val element = webDriver.findElement(By.cssSelector(selectorData.selector))
            element.sendKeys(System.getProperty("user.dir") ++ selectorData.value)
          case "checkbox" =>
            val element = webDriver.findElement(By.cssSelector(selectorData.selector))
            selectorData.value  match {
              case "true"  if !element.isSelected  => element.click()
              case "false" if element.isSelected   => element.click()
              case _ => ()
            }
        }
      }
      )
  }

  And("^I click the (.*) element$") {
    (selector: String) =>
      val clickableElement = webDriver.findElement(By.cssSelector(selector))
      clickableElement.click()
  }

  And("I sleep (\\d+) seconds") {
    (time : Integer) =>
      Thread.sleep(time*1000)
  }

  var scenarioData = scala.collection.mutable.Map[String, String]()

  Then("the consignment id from the url") {
    val url = webDriver.getCurrentUrl
    val paramsData: Map[String, String] = new URL(url).getQuery.split("&").map { s =>
      val pair = s.split('=')
      pair(0) -> pair(1)
    }.toMap
    scenarioData ++= paramsData
    () //must return unit
  }



  Then("^I expect the database query (.*) to contain:$") {
    (query: String, dataTable: DataTable) =>
      val expectedData = dataTable.asList(classOf[DatabaseResponseData]).asScala.toList
      val actualData = runAndUnpackQuery(query, scenarioData("consignmentId").toInt, graphQlClient)
      assertOnFields(expectedData, actualData)
  }

}


case class FieldInsertData(selector: String, value: String, fieldType: String)
case class DatabaseResponseData(key: String, value: String)