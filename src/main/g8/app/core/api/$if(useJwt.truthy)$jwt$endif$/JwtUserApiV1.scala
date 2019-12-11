package core.api.jwt

import com.google.inject.ImplementedBy
import core.api.Role
import hr.laplacian.laplas.commons.error.Eor
import play.api.libs.json.{ Format, Json }

import scala.concurrent.Future

object JwtUserApiV1 {

  @ImplementedBy(classOf[JwtUserApiV1Impl])
  trait Service {
    def login(login: Contract.Login): Future[Eor[Option[Contract.UserIdentity]]]
  }

  object Contract {
    case class Login(username: String, password: String)
    case class UserIdentity(id: String, username: String, role: Role)

    implicit val jsonLoginFormat: Format[Contract.Login] = Json.format[Contract.Login]
    implicit val jsonUserIdentityFormat: Format[Contract.UserIdentity] = Json.format[Contract.UserIdentity]
  }
}
