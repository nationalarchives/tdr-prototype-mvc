import com.mohiva.play.silhouette.api.Identity


package object auth {

  case class User(id: Int,
                  firstName: String,
                  lastName: String,
                  email: String,
                  providerID: String,
                  providerKey: String) extends Identity


}
