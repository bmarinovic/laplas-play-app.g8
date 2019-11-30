package core.jwt

import java.time.Instant

import core.jwt.JwtApiV1.Contract
import hr.laplacian.laplas.commons.error.{Eor, SimpleUuidError}
import io.circe.Decoder
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import tsec.common.SecureRandomId
import tsec.jws.mac.JWTMacImpure
import tsec.jwt.JWTClaims
import tsec.mac.jca.{HMACSHA256, MacSigningKey}

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class JwtApiV1Service @Inject
(
  configuration: Configuration
) extends JwtApiV1.Service {

  private val authConfig = configuration.get[Configuration]("auth")
  private val refreshTokenTTL = authConfig.get[FiniteDuration]("refreshTokenTTL")
  private val accessTokenTTL = authConfig.get[FiniteDuration]("accessTokenTTL")
  private val signingSecret = authConfig.get[String]("secret")

  private val jwtDataKey = "data"

  override def buildTokenPair(jwtData: Contract.JwtData, jti: Option[String]): Future[Eor[Contract.JwtTokenPair]] = {
    val refreshTokenClaim: JWTClaims = JWTClaims(
      jwtId = jti.orElse(Some(SecureRandomId.Interactive.generate)),
      expiration = Some(Instant.now.plusSeconds(refreshTokenTTL.toSeconds)),
      customFields = Seq(("userId", jwtData.userId.asJson))
    )

    val accessTokenClaim = JWTClaims(
      jwtId = refreshTokenClaim.jwtId,
      expiration = Some(Instant.now.plusSeconds(accessTokenTTL.toSeconds)),
      customFields = Seq((jwtDataKey, jwtData.asJson)))

    val accessToken = JWTMacImpure.buildToString[HMACSHA256](accessTokenClaim, signingKey)
    val refreshToken = JWTMacImpure.buildToString[HMACSHA256](refreshTokenClaim, signingKey)

    (accessToken, refreshToken) match {
      case (Right(at), Right(rt)) => Future.successful(Right(Contract.JwtTokenPair(at, rt)))
      case _ => Future.successful(Left(SimpleUuidError("JWT_FAILED_TO_CONSTRUCT")))
    }
  }

  private def signingKey: MacSigningKey[HMACSHA256] = HMACSHA256.unsafeBuildKey(signingSecret.getBytes)

  override def getRefreshTokenClaim(refreshToken: String): Future[Eor[Contract.RefreshTokenClaim]] = {
    JWTMacImpure.verifyAndParse[HMACSHA256](refreshToken, signingKey)
      .map(_.body)
      .map(claim => {
        (claim.jwtId, claim.getCustom[String]("userId")) match {
          case (Some(jti), Right(id)) => Right(Contract.RefreshTokenClaim(jti, id))
          case _ => Left(SimpleUuidError("JWT_CLAIM_ERROR"))
        }
      }) match {
      case Right(Right(s)) => Future.successful(Right(s))
      case _ => Future.successful(Left(SimpleUuidError("JWT_PARSE_ERROR")))
    }
  }

  override def introspect(jwtToken: String): Future[Eor[Contract.JwtData]] = {
    JWTMacImpure.verifyAndParse[HMACSHA256](jwtToken, signingKey)
      .map(_.body)
      .map(_.getCustom(jwtDataKey)(Decoder[Contract.JwtData])) match {
      case Right(Right(s)) => Future.successful(Right(s))
      case _ => Future.successful(Left(SimpleUuidError("JWT_INTROSPECT_ERROR")))
    }
  }
}
