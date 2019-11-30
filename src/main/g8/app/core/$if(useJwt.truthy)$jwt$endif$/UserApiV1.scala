package core.jwt

import core.api.Role
import play.api.libs.json.{Format, Json}

import scala.concurrent.Future

object UserApiV1 {
  trait Service {
    def login(username: String, password: String): Future[Option[Contract.UserIdentity]]
  }

  object Contract {
    case class Login(username: String, password: String)
    case class UserIdentity(id: String, username: String, role: Role)

    implicit val jsonLoginFormat: Format[Contract.Login] = Json.format[Contract.Login]
    implicit val jsonUserIdentityFormat: Format[Contract.UserIdentity] = Json.format[Contract.UserIdentity]
  }
}
