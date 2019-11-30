package core.api

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsString, Reads, Writes}

trait DateTimeCoder {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val jodaDateReads: Reads[DateTime] = Reads[DateTime](
    _.validate[String].map[DateTime](DateTime.parse(_, DateTimeFormat.forPattern(dateFormat)))
  )
  implicit val jodaDateWrites: Writes[DateTime] = (d: DateTime) => JsString(d.toString())
}
