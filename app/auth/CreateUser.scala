package auth

case class CreateUser(name: String, username: String, password: String, confirmPassword: String)
