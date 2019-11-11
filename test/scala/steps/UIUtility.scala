package scala.steps

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.remote.RemoteWebDriver


object UIUtility {

  private var webDriver : Option[RemoteWebDriver] = None
  private val getWebDriverOption: Option[RemoteWebDriver] = {
    System.getenv("CHROME_DRIVER") match {
      case value : String if value.nonEmpty =>
        if (webDriver.isEmpty) {
          val chromeOptions = new ChromeOptions
//          chromeOptions.addArguments("--headless")
          System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver")
          webDriver = Some(new ChromeDriver(chromeOptions))
        }
      case _ => throw new RuntimeException("please set CHROME_DRIVER property to install location of chromedriver")
    }
    webDriver
  }
  val getWebDriver: RemoteWebDriver = getWebDriverOption.get

}
