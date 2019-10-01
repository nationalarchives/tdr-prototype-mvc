package viewsapi

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

    def requiredInput() = {
      if(args.contains(requiredInputArg)) {
        "required"
      }
    }
  }

  implicit def inputRenderOptions(args: Map[Symbol, Any]): InputRenderOptions = new InputRenderOptions(args)
}
