package controllers

import com.mohiva.play.silhouette.api.Silhouette
import graphql.GraphQLClientProvider
import graphql.codegen.CreateConsignment.createConsignment
import javax.inject.{Inject, _}
import model.CreateConsignmentData
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.{Pac4jScalaTemplateHelper, Security, SecurityComponents}
import play.api.Configuration
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateConsignmentController @Inject()( client: GraphQLClientProvider,
                                             val controllerComponents: SecurityComponents,
                                             configuration: Configuration)(
                                             implicit val ex: ExecutionContext,
                                             implicit val pac4jTemplateHelper: Pac4jScalaTemplateHelper[CommonProfile]) extends  Security[CommonProfile]  with play.api.i18n.I18nSupport {



  def index(seriesId:Int) = Action.async { implicit request: Request[AnyContent] =>

    Future(Ok(views.html.createConsignments(CreateConsignmentData.form, seriesId)))
  }

  def submit() = Action.async { implicit request =>
    val graphQlClient = client.graphqlClient
    val form = CreateConsignmentData.form.bindFromRequest
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profile = profileManager.get(true).get().asInstanceOf[OidcProfile]
    val vars = createConsignment.Variables(form.get.consignmentName,form.get.seriesId,profile.getEmail,form.get.transferringBody)

    graphQlClient.query[createConsignment.Data, createConsignment.Variables](createConsignment.document, vars).result.map {
      case Right(r) =>
        Redirect(routes.UploadController.index(r.data.createConsignment.id, r.data.createConsignment.series.id))
      case Left(e) => InternalServerError(e.errors.toString())
    }
  }
}
