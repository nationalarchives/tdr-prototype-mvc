package controllers

import auth.LoginForm.LoginData
import auth.{LoginForm, SignUpForm, UserDao}
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.services.{AuthenticatorResult, AuthenticatorService}
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AuthController @Inject()(controllerComponents: ControllerComponents,
                               userService: UserDao,
                               silhouette: Silhouette[DefaultEnv],
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


  def signUpForm: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signup(SignUpForm.form)))
  }

  def signUpSubmit: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

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


}