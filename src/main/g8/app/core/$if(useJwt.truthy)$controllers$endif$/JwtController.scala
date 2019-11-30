package core.controllers

import core.api.{AdminRole, Role, UserRole}
import core.jwt.{JwtApiV1, UserApiV1}
import hr.laplacian.laplas.commons.error.SimpleUuidError
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JwtController @Inject()
(
  cc: ControllerComponents,
  jwtApiV1Service: JwtApiV1.Service
)(implicit executionContext: ExecutionContext)
  extends AbstractController(cc) {

  var userRepository = new mutable.HashMap[UserApiV1.Contract.Login, Role]()
  userRepository += (UserApiV1.Contract.Login("test", "test") -> UserRole)
  userRepository += (UserApiV1.Contract.Login("admin", "admin") -> AdminRole)

  def login = Action(parse.json[UserApiV1.Contract.Login]).async { req =>
    userRepository.zipWithIndex.find { case ((login, _), _) => req.body == login } match {
      case Some(((login, role), pos)) =>
        val jwtData = JwtApiV1.Contract.JwtData(pos.toString, login.username, role)
        jwtApiV1Service.buildTokenPair(jwtData)
          .map {
            case Left(value) => BadRequest(Json.obj("error" -> value.description))
            case Right(value) => Ok(Json.toJson(value))
          }
      case None => Future.successful(BadRequest(Json.obj("error" -> SimpleUuidError("INVALID_USERNAME_OR_PASSWORD").description)))
    }
  }

  def refreshToken = Action(parse.json[JwtApiV1.Contract.RefreshToken]).async { req =>
    jwtApiV1Service.getRefreshTokenClaim(req.body.refreshToken)
      .flatMap {
        case Left(value) => Future.successful(BadRequest(Json.obj("error" -> value.description)))
        case Right(value) =>
          userRepository.zipWithIndex.find(_._2.toString == value.userId) match {
            case Some(((login, role), _)) =>
              val jwtData = JwtApiV1.Contract.JwtData(value.userId, login.username, role)
              jwtApiV1Service.buildTokenPair(jwtData, Some(value.jti))
                .map {
                  case Left(e) => BadRequest(Json.obj("error" -> e.description))
                  case Right(v) => Ok(Json.toJson(v))
                }
            case None => Future.successful(NotFound(Json.obj("error" -> "USER_NOT_FOUND")))
          }
      }
  }
}
