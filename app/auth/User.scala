package auth

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

/**
  * The user object.
  *
  * @param loginInfo The linked login info.
  */
case class User(loginInfo: LoginInfo) extends Identity {}
