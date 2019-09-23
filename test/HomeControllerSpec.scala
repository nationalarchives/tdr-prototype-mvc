import controllers.HomeController
import org.scalatestplus.play._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.test.WithApplication

import scala.concurrent.{ExecutionContext, Future}


class HomeControllerSpec extends PlaySpec with Results {


//    "Home controller#index" should {
//      "show the sign in button" in new WithApplication() {
//        val application: Application = GuiceApplicationBuilder().build()
//        val controller: HomeController = application.injector.instanceOf[HomeController]
//        val executionContext: ExecutionContext = application.injector.instanceOf[ExecutionContext]
//        val resultOption: Option[Future[Result]] = route(application, FakeRequest(GET, "/"))
//        val result = resultOption.get
//        val text: String = contentAsString(result)
//        status(result) mustEqual 200
//        text.contains("Sign in") mustBe true
//      }
//    }

}