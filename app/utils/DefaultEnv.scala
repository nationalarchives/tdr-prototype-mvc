package utils

import auth.Auth0User
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

trait DefaultEnv extends Env {
  type I = Auth0User
  type A = CookieAuthenticator
}
