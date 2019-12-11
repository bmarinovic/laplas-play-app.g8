package core.api.jwt

import core.api.AdminRole
import core.api.jwt.JwtUserApiV1.Contract
import core.api.jwt.JwtUserApiV1.Contract.UserIdentity
//import $organization$.$name$.api.users.UsersApiV1
import hr.laplacian.laplas.commons.error.Eor
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JwtUserApiV1Impl @Inject()(
//    usersApiV1Service: UsersApiV1.Service
)(implicit ec: ExecutionContext)
    extends JwtUserApiV1.Service {

  override def login(login: Contract.Login): Future[Eor[Option[Contract.UserIdentity]]] =
    Future.successful(Right(Some(UserIdentity(1.toString, "admin@laplacian.hr", AdminRole))))
  // Should provide method something like this
  //    usersApiV1Service
  //      .findByEmailAndSecret(login.username, login.password)
  //      .map(_.map(_.map(user => UserIdentity(user.id.toString, login.username, user.role))))
}
