package core.controllers

import core.api.Role
import core.actions.ActionProvider
import core.api.jwt.JwtUserApiV1.Contract.UserIdentity
import core.api.jwt.{ JwtApiV1, JwtUserApiV1 }
//import $organization$.$name$.api.users.UsersApiV1
import hr.laplacian.laplas.commons.error.SimpleUuidError
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http.HeaderNames

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

@Singleton
class JwtController @Inject()(
    cc: ControllerComponents,
    jwtApiV1Service: JwtApiV1.Service,
    jwtUserApiV1: JwtUserApiV1.Service,
//    usersApiV1: UsersApiV1.Service,
    actionProvider: ActionProvider
)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc) {

  final private val BEARER = "(b|B)earer"

  def login: Action[JwtUserApiV1.Contract.Login] = Action(parse.json[JwtUserApiV1.Contract.Login]).async { req =>
    jwtUserApiV1
      .login(req.body)
      .flatMap {
        case Right(optionUser) =>
          optionUser match {
            case Some(UserIdentity(id, username, role)) =>
              val jwtData = JwtApiV1.Contract.JwtData(id, username, role)
              jwtApiV1Service
                .buildTokenPair(jwtData)
                .map {
                  case Left(value)  => BadRequest(Json.obj("error" -> value.description))
                  case Right(value) => Ok(Json.toJson(value))
                }
            case None =>
              Future.successful(
                BadRequest(Json.obj("error" -> SimpleUuidError("INVALID_USERNAME_OR_PASSWORD").description))
              )
          }
        case Left(value) => Future.successful(BadRequest(Json.obj("error" -> value.description)))
      }
  }

  def refreshToken: Action[JwtApiV1.Contract.RefreshToken] = ???
//    Action(parse.json[JwtApiV1.Contract.RefreshToken]).async {
//    req =>
//      jwtApiV1Service
//        .getRefreshTokenClaim(req.body.refreshToken)
//        .flatMap {
//          case Left(value) => Future.successful(BadRequest(Json.obj("error" -> value.description)))
//          case Right(value) =>
//            Try(value.userId.toLong) match {
//              case Success(userId) =>
//                usersApiV1.findById(userId).flatMap {
//                  case Right(optionUser) =>
//                    optionUser match {
//                      case Some(user) =>
//                        val jwtData =
//                          JwtApiV1.Contract.JwtData(value.userId, user.email, Role.dbStringToRole(user.role))
//                        jwtApiV1Service
//                          .buildTokenPair(jwtData, Some(value.jwtId))
//                          .map {
//                            case Left(e)  => BadRequest(Json.obj("error" -> e.description))
//                            case Right(v) => Ok(Json.toJson(v))
//                          }
//                      case None => Future.successful(NotFound(Json.obj("error" -> "USER_NOT_FOUND")))
//                    }
//                  case Left(e) => Future.successful(BadRequest(Json.obj("error" -> e.description)))
//                }
//              case Failure(e) => Future.successful(BadRequest(Json.obj("error" -> e.getMessage)))
//            }
//        }
//  }

  def introspect: Action[AnyContent] = actionProvider.SecureAction.async { request =>
    request.headers.get(HeaderNames.AUTHORIZATION).flatMap(_.split(BEARER).drop(1).headOption.map(_.trim)) match {
      case Some(token) =>
        jwtApiV1Service
          .introspect(token)
          .map {
            case Right(data) => Ok(Json.toJson(data))
            case Left(e)     => BadRequest(Json.obj("error" -> e.description))
          }
      case None => Future.successful(play.api.mvc.Results.Unauthorized)
    }
  }

}
