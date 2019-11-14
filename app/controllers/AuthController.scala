package controllers

import forms.LoginForm.LoginData
import auth._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{AuthenticatorResult, AuthenticatorService}
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, GoogleTotpInfo}
import com.sendgrid.{Content, Email, Mail, Method, SendGrid}
import forms.{LoginForm, ResetPasswordEmailForm, ResetPasswordForm, SignUpForm, TotpForm}
import graphql.GraphQLClientProvider
import graphql.codegen.IsPasswordTokenValid.isPasswordTokenValid
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.data.Form
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AuthController @Inject()(controllerComponents: ControllerComponents,
                               userService: UserService,
                               uzerZervice: UzerZervice,
                               silhouette: Silhouette[DefaultEnv],
                               client: GraphQLClientProvider,
                               config: Configuration,
                               credentialsProvider: CredentialsProvider)(implicit executionContext: ExecutionContext,
                                                                         authInfoRepository: AuthInfoRepository) extends AbstractController(controllerComponents)  with play.api.i18n.I18nSupport {

  private val environment: String = config.get[String]("app.environment")

  val authService: AuthenticatorService[CookieAuthenticator] = silhouette.env.authenticatorService

  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(LoginForm.form))
  }

  def logout: Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
    authService.discard(request.authenticator, Redirect(routes.HomeController.index()))
  }

  def processLoginAttempt: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    val errorFunction: Form[LoginData] => Future[Result] = { formWithErrors: Form[LoginData] =>
      Future.successful(BadRequest(views.html.login(formWithErrors)))
    }

    val successFunction: LoginData => Future[Result] = { user: LoginData =>


      val magicTokenForIan: String = uzerZervice.retrieve(user).idToken


      credentialsProvider.authenticate(credentials = Credentials(user.username, user.password))
        .flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(u) => authInfoRepository.find[GoogleTotpInfo](loginInfo).flatMap {
              case Some(totpInfo) => Future.successful(Ok(views.html.totp(TotpForm.form.fill(TotpForm.Data(u.email, totpInfo.sharedKey)))))
              case _ =>  authService.create(loginInfo)
                .flatMap(authService.init(_))
                .flatMap(authService.embed(_, Redirect(routes.DashboardController.index())))
            }
            case None => Future.successful(Redirect(routes.AuthController.login()))
          }
        }.recover {
        case e: Exception =>
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
        uzerZervice.createNewUser(success) //sneak in call to demo code
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
      userService.retrieve(LoginInfo(CredentialsProvider.ID, resetForm.email)).flatMap {
        case Some(user) =>
        userService.createOrUpdatePasswordResetToken(resetForm.email)
          .map {
            token =>
              val from = new Email("test@example.com")
              val subject = "Reset your password"
              val to = new Email(user.email)
              val url = if(environment.equals("dev")) {"https://app.tdr-prototype.co.uk"} else {"http://localhost:9000"}
              val content = new Content("text/html", s"<h1>$url/resetPassword?email=${resetForm.email}&token=$token</h1>")
              val mail = new Mail(from, subject, to, content)
              Redirect(routes.AuthController.login())

          }
        case _ => Future.successful(Redirect(routes.AuthController.resetPasswordEmail()).flashing("email-error" -> "Email does not exist"))
      }

    }

    val formValidationResult: Form[ResetPasswordEmailForm.Data] = ResetPasswordEmailForm.form.bindFromRequest

    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  def resetPassword(email: String, token: String) = Action.async { implicit request =>
    val graphqlClient = client.graphqlClient
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