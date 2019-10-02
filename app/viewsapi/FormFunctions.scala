package viewsapi

import play.twirl.api.Html
import views.html.helper.FieldElements

object FormFunctions {

  val selectedInputArg = '_checkedOption
  val disabledInputArg = '_disabledOption
  val requiredInputArg = '_requiredOption

  class InputRenderOptions(args: Map[Symbol, Any]) {

    def selectedInput(value: String) = {
      if(args.contains(selectedInputArg)) {
        args.get(selectedInputArg).filter( _ == value).map{_ => "checked"}
      }
    }

    def disabledInput(value: String) = {
      if(args.contains(disabledInputArg)) {
        args.get(disabledInputArg).filter( _ == value).map{_ => "disabled"}
      }
    }

    def requiredLabelSuffix() = {
      if(args.contains(requiredInputArg)) {
        "*"
      }
    }

    def requiredInput() = {
      if(args.contains(requiredInputArg)) {
        "required"
      }
    }
  }

  class ErrorHandling(elements: FieldElements) {

    def setErrorClass() = {
      if(elements.hasErrors) {
        "govuk-form-group--error"
      }
    }

    def renderErrorMessage() = {
      if(elements.hasErrors) {
        Html("<span id=\"error\" class=\"govuk-error-message\"><span class=\"govuk-visually-hidden\">Error:</span>" +
          elements.errors.mkString(", ")
        )
      }
    }
  }

  implicit def inputRenderOptions(args: Map[Symbol, Any]): InputRenderOptions = new InputRenderOptions(args)
  implicit def errorHandling(elements: FieldElements): ErrorHandling = new ErrorHandling(elements)
}
