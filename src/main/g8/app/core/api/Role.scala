package core.api

import play.api.libs.json._

trait Role {
  val level: Int
  val name: String
}

case object NoRole extends Role {
  val level = 0
  val name = "None"
}

case object UserRole extends Role {
  val level = 100
  val name = "User"
}

case object AdminRole extends Role {
  val level = 1000
  val name = "Admin"
}

object Role {
  implicit val reads: Reads[Role] = (json: JsValue) => {
    json.validate[String].getOrElse(JsError("role is not supported")) match {
      case "User" => JsSuccess(UserRole)
      case "Admin" => JsSuccess(AdminRole)
      case _ => JsError(s"Invalid Role: \$json")
    }
  }

  implicit val writes: Writes[Role] = (o: Role) => JsString(o.name)

  //  // circe
  import io.circe.HCursor
  import io.circe.Encoder
  import io.circe.syntax._
  import io.circe.Decoder

  implicit val encodeEvent: Encoder[Role] = Encoder.instance {
    case noRole @ NoRole => noRole.name.asJson
    case userRole @ UserRole => userRole.name.asJson
    case adminRole @ AdminRole => adminRole.name.asJson
  }

  implicit val decodeFoo: Decoder[Role] = (c: HCursor) => {
    c.as[String] match {
      case Right("Admin") => Right(AdminRole)
      case Right("User") => Right(UserRole)
      case Right(r) =>
        println(s"Unknown role: \$r")
        Right(NoRole)
      case Left(e) =>
        println(s"Failed to decode role: \$e")
        Right(NoRole)
    }
  }

  val dbStringToRole: Map[String, Role] = Map(
    AdminRole.name -> AdminRole,
    UserRole.name  -> UserRole
  )
}
