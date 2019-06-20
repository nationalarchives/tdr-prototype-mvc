package auth

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import providers.CognitoSocialProfile

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(implicit ex: ExecutionContext) extends IdentityService[User] {

  def retrieve(id: UUID) = Future.successful(UserService.users.get(id))

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    Future.successful(UserService.users.find { case (_, user) => user.loginInfo == loginInfo }.map(_._2))

  def save(user: User) = {
    UserService.save(user)
    Future.successful(user)
  }

  def save(profile: CognitoSocialProfile): Future[User] = {
    retrieve(profile.loginInfo).map {
      case Some(user) => // Update user with profile
        UserService.save(user)
        user
      case None => // Insert a new user
        val user = User(
          userID = UUID.randomUUID(),
          loginInfo = profile.loginInfo,
          firstName = None,
          lastName = None,
          fullName = None,
          email = None,
          avatarURL = None,
          activated = true
        )
        UserService.save(user)
        user
    }
  }
}

object UserService {
  val users: mutable.HashMap[UUID, User] = mutable.HashMap()

  def save(user: User): Unit = {
    users += (user.userID -> user)
  }
}
