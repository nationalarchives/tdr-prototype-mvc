package controllers

import auth.LoginForm.LoginData
import auth.{LoginForm, ResetPasswordEmailForm, ResetPasswordForm, SignUpForm, UserDao}
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model._
import com.mohiva.play.silhouette.api.services.{AuthenticatorResult, AuthenticatorService}
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import graphql.GraphQLClientProvider
import graphql.codegen.IsPasswordTokenValid.isPasswordTokenValid
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class AuthController @Inject()(controllerComponents: ControllerComponents,
                               userService: UserDao,
                               silhouette: Silhouette[DefaultEnv],
                               client: GraphQLClientProvider,
                               credentialsProvider: CredentialsProvider)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents)  with play.api.i18n.I18nSupport {

  val authService: AuthenticatorService[CookieAuthenticator] = silhouette.env.authenticatorService


  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(LoginForm.form))
  }

  def logout: Action[AnyContent] = Action {
    Redirect(routes.AuthController.login()).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  def processLoginAttempt: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val errorFunction: Form[LoginData] => Future[Result] = { formWithErrors: Form[LoginData] =>
      Future.apply(BadRequest(views.html.login(formWithErrors)))
    }
    val successFunction: LoginData => Future[Result] = { user: LoginData =>
      credentialsProvider.authenticate(credentials = Credentials(user.username, user.password))
        .flatMap { loginInfo =>


          authService.create(loginInfo)
            .flatMap(authService.init(_))
            .flatMap(authService.embed(_, Redirect(routes.DashboardController.index())))

        }.recover {
        case e: Exception =>
          e.printStackTrace()
          Redirect(routes.AuthController.login()).flashing("login-error" -> e.getMessage)
      }
    }

    val formValidationResult: Form[LoginData] = LoginForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )

  }


  def signUpForm: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signup(SignUpForm.form))
  }

  def signUpSubmit: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    SignUpForm.form.bindFromRequest.fold(
      (hasErrors: Form[SignUpForm.Data]) =>
        Future.successful(BadRequest(views.html.signup(hasErrors))),

      (success: SignUpForm.Data) => {

        userService.retrieve(LoginInfo(CredentialsProvider.ID, success.email))
          .flatMap((uo: Option[auth.User]) =>
            uo.fold({
              userService.create(success).flatMap(authService.create(_))
                .flatMap(authService.init(_))
                .flatMap(authService.embed(_, Redirect(routes.HomeController.index())))

            })({ _ =>
              Future.successful(AuthenticatorResult(Redirect(routes.AuthController.signUpForm())))
            }))
      })
  }

  def resetPasswordEmail = Action {implicit request =>
    Ok(views.html.resetPasswordEmail(ResetPasswordEmailForm.form))
  }

  def sendResetPasswordEmail = Action.async { implicit request =>
    val errorFunction: Form[ResetPasswordEmailForm.Data] => Future[Result] = { formWithErrors: Form[ResetPasswordEmailForm.Data] =>
      Future.apply(BadRequest(views.html.resetPasswordEmail(formWithErrors)))
    }

    val successFunction: ResetPasswordEmailForm.Data => Future[Result] = { resetForm: ResetPasswordEmailForm.Data =>
      userService.createOrUpdatePasswordResetToken(resetForm.email)
          .map {
            token =>
              val client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_WEST_1).build()
              val request = new SendEmailRequest()
                                .withDestination(new Destination().withToAddresses(resetForm.email))
                                .withMessage(new Message().withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(s"<h1>$token</h1>")))
                                .withSubject(new Content().withCharset("UTF-8").withData("Test"))
                                ).withSource("noreply@tdr-prototype.co.uk")
              client.sendEmail(request)
              Redirect(routes.AuthController.login())
          }
    }

    val formValidationResult: Form[ResetPasswordEmailForm.Data] = ResetPasswordEmailForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  def resetPassword(email: String, token: String) = Action.async { implicit request =>
    val graphqlClient = client.graphqlClient(List())
    val vars = isPasswordTokenValid.Variables(email, token)
    graphqlClient.query[isPasswordTokenValid.Data, isPasswordTokenValid.Variables](isPasswordTokenValid.document, vars).result.map {
      case Right(r) if r.data.isPasswordTokenValid =>  Ok(views.html.resetPassword(ResetPasswordForm.form, email))
      case Right(_) => Redirect(routes.AuthController.login())
    }
  }

  def submitResetPassword(email: String) = Action.async { implicit request =>
    val errorFunction: Form[ResetPasswordForm.Data] => Future[Result] = { formWithErrors: Form[ResetPasswordForm.Data] =>
      Future.apply(BadRequest(views.html.resetPassword(formWithErrors, email)))
    }

    val successFunction: ResetPasswordForm.Data => Future[Result] = { resetForm: ResetPasswordForm.Data =>
      userService.updatePassword(email, resetForm.newPassword) map {
        _ => Redirect(routes.AuthController.login())
      }
    }

    val formValidationResult: Form[ResetPasswordForm.Data] = ResetPasswordForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }


}