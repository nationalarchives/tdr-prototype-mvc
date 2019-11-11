package scala.runner


import org.junit.runner.RunWith
import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("classpath:features/"),
  tags = Array("not @Wip", "not @Integration"),
  glue = Array("classpath:scala/steps/"),
  plugin = Array("pretty", "html:target/cucumber/html"))
class FeatureRunnerUI {

}


