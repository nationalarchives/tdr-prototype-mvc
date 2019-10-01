package auth

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

object SignUpForm {

  val complexPassword = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"r

  val passwordCheckConstraint: Constraint[String] = Constraint(s"Must match ${complexPassword.pattern}")({ plainText =>
    val errors = plainText match {
      case complexPassword() => Nil
      case _ => Seq(ValidationError(s"Password must match ${complexPassword.toString}"))
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

