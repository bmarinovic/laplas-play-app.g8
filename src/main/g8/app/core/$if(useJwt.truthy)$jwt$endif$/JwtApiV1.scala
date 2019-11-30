package core.jwt

import com.google.inject.ImplementedBy
import core.api.Role
import hr.laplacian.laplas.commons.error.Eor
import io.circe._
import io.circe.generic.semiauto._
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.Future

object JwtApiV1
{
  @ImplementedBy(classOf[JwtApiV1Service])
  trait Service
  {
    def buildTokenPair(userIdentity: Contract.JwtData, jti: Option[String] = None): Future[Eor[Contract.JwtTokenPair]]
    def getRefreshTokenClaim(refreshToken: String): Future[Eor[Contract.RefreshTokenClaim]]
    def introspect(jwtToken: String): Future[Eor[Contract.JwtData]]
  }

  object Contract
  {
    case class RefreshToken(refreshToken: String)
    case class RefreshTokenClaim(jti: String, userId: String)
    case class JwtTokenPair(accessToken: String, refreshToken: String)
    case class JwtData(userId: String, username: String, role: Role)

    implicit val jsonRefreshTokenFormat: OFormat[RefreshToken] = Json.format[RefreshToken]
    implicit val jsonRefreshTokenClaimFormat: OFormat[RefreshTokenClaim] = Json.format[RefreshTokenClaim]
    implicit val jsonJwtTokenPairFormat: OFormat[JwtTokenPair] = Json.format[JwtTokenPair]
    implicit val jsonJwtDataFormat: OFormat[JwtData] = Json.format[JwtData]

    implicit val encoder: ObjectEncoder[JwtData] = deriveEncoder[JwtData]
    implicit val decoder: Decoder[JwtData]       = deriveDecoder[JwtData]
  }
}
