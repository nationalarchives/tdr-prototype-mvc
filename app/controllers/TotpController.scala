package controllers

import auth.UserService
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, GoogleTotpInfo, GoogleTotpProvider}
import forms.{TotpForm, TotpSetupForm}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.Messages
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class TotpController @Inject ()(silhouette: Silhouette[DefaultEnv],
                                totpProvider: GoogleTotpProvider,
                                cc: ControllerComponents,
                                userService: UserService,
                                configuration: Configuration
                               )(
  implicit ec: ExecutionContext,
  authInfoRepository: AuthInfoRepository
) extends AbstractController(cc)  with play.api.i18n.I18nSupport {

  val authService: AuthenticatorService[CookieAuthenticator] = silhouette.env.authenticatorService


  def view = Action.async { implicit request =>
    Future.successful(Ok(views.html.totp(TotpForm.form)))
  }

  def submit = Action.async { implicit request =>
    TotpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.totp(form))),
      data => {
        userService.retrieve(LoginInfo(CredentialsProvider.ID, data.userID.toString)).flatMap {
          case Some(user) =>
            totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
              case Some(_) =>
                val loginInfo = LoginInfo(CredentialsProvider.ID, user.email)
                userService.retrieve(loginInfo)
                //
                authService.create(loginInfo)
                  .flatMap(authService.init(_))
                  .flatMap(authService.embed(_, Redirect(routes.DashboardController.index())))
              case _ => Future.successful(Redirect(routes.TotpController.view()).flashing("error" -> Messages("invalid.verification.code")))
            }.recover {
              case _: ProviderException =>
                Redirect(routes.AuthController.login()).flashing("error" -> Messages("invalid.unexpected.totp"))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
    )
  }

  def enableTotp = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    val credentials = totpProvider.createCredentials(user.email)
    val totpInfo = credentials.totpInfo
    val formData = TotpSetupForm.form.fill(TotpSetupForm.Data(totpInfo.sharedKey, totpInfo.scratchCodes, credentials.scratchCodesPlain))
    authInfoRepository.find[GoogleTotpInfo](LoginInfo(CredentialsProvider.ID, user.email)).map { totpInfoOpt =>
      Ok(views.html.enableTotp(user, totpInfoOpt, Some((formData, credentials))))
    }
  }

  def enableTotpSubmit = silhouette.SecuredAction.async { implicit request =>
    val user = request.identity
    val loginInfo = LoginInfo(CredentialsProvider.ID, user.email)
    TotpSetupForm.form.bindFromRequest.fold(
      form => authInfoRepository.find[GoogleTotpInfo](loginInfo).map { totpInfoOpt =>
        BadRequest(views.html.dashboard())
      },
      data => {
        totpProvider.authenticate(data.sharedKey, data.verificationCode).flatMap {
          case Some(_: LoginInfo) =>
            authInfoRepository.add[GoogleTotpInfo](loginInfo, GoogleTotpInfo(data.sharedKey, data.scratchCodes))
            Future(Redirect(routes.DashboardController.index()).flashing("success" -> Messages("totp.enabling.info")))
          case _ => Future.successful(Redirect(routes.DashboardController.index()).flashing("error" -> Messages("invalid.verification.code")))
        }.recover {
          case _: ProviderException =>
            Redirect(routes.TotpController.view()).flashing("error" -> Messages("invalid.unexpected.totp"))
        }
      }
    )
  }

}
