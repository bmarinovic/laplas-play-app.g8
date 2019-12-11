package core.actions

import com.google.inject.Inject
import core.api.Role
import core.api.jwt.JwtApiV1
import javax.inject.Singleton
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class DefaultActionBuilder
(
  val parser: BodyParsers.Default
)(implicit ec: ExecutionContext) extends ActionBuilder[Request, AnyContent] {
  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    block(request)
  }
}


class UserRequest[A](val userId: String, val role: Role, val username: String, request: Request[A]) extends WrappedRequest[A](request)

class JwtAction
(
  jwtApiV1Service: JwtApiV1.Service
)(implicit ec: ExecutionContext) extends ActionRefiner[Request, UserRequest] {
  private final val AUTHORIZATION = "Authorization"
  private final val BEARER = "(b|B)earer"

  override protected def executionContext: ExecutionContext = ec

  override protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    request.headers.get(AUTHORIZATION).flatMap(_.split(BEARER).drop(1).headOption.map(_.trim)) match {
      case Some(token) =>
        jwtApiV1Service.introspect(token)
          .map(_.map(data => new UserRequest(data.userId, data.role, data.username, request)))
          .map(_.left.map(_ => play.api.mvc.Results.Unauthorized))
      case None => Future.successful(Left(play.api.mvc.Results.Unauthorized))
    }
  }
}

class StaticRoleAction
(
  role: Role
)(implicit val ec: ExecutionContext)
  extends ActionFilter[UserRequest] {
  override protected def executionContext: ExecutionContext = ec

  override protected def filter[A](request: UserRequest[A]): Future[Option[Result]] = {
    val result = if (request.role.level >= role.level) None else Some(play.api.mvc.Results.Forbidden)
    Future.successful(result)
  }
}

@Singleton
class ActionProvider @Inject()
(
  parser: BodyParsers.Default,
  jwtApiV1Service: JwtApiV1.Service
)(implicit ec: ExecutionContext) {
  def SecureAction: ActionBuilder[UserRequest, AnyContent] =
    new DefaultActionBuilder(parser) andThen
      new JwtAction(jwtApiV1Service)

  def SecureActionWithRole(role: Role): ActionBuilder[UserRequest, AnyContent] =
    new DefaultActionBuilder(parser) andThen
      new JwtAction(jwtApiV1Service) andThen
      new StaticRoleAction(role)
}

