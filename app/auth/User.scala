package auth

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

/**
  * The user object.
  *
  * @param userID The unique ID of the user.
  * @param loginInfo The linked login info.
  */
case class User(userID: UUID, loginInfo: LoginInfo) extends Identity {}
