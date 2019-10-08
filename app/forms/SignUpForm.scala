package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text, nonEmptyText, email}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object SignUpForm {

  val complexPassword = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"r

  val passwordCheckConstraint: Constraint[String] = Constraint(s"Must match ${complexPassword.pattern}")({ plainText =>
    val errors = plainText match {
      case complexPassword() => Nil
      case _ => Seq(ValidationError(s"Password needs to be at least 8 characters long and include one lowercase letter, one uppercase letter, one digit and one special character (! @ $$ % & * ?)."))
    }
    if (errors.isEmpty) {
      Valid
    } else {
      Invalid(errors)
    }
  })


  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> text.verifying(passwordCheckConstraint)
    )(Data.apply)(Data.unapply)
  )

  case class Data(firstName: String,
                  lastName: String,
                  email: String,
                  password: String)

}
