package core.api

import play.api.libs.json.{Json, OFormat}

case class Pagination
(
  pageNumber      : Int,
  pageSize        : Int,
  pageItemsOffset : Int = 0
)
{
  private val virtualOffset = pageSize * (pageNumber - 1) + pageItemsOffset
  lazy val from, offset: Int = Math.max(0, virtualOffset)
  lazy val to: Int = virtualOffset + pageSize
  lazy val limit : Int = pageSize + Math.min(0, virtualOffset)
}

object Pagination
{
  final val ALL : Pagination = Pagination(1, Integer.MAX_VALUE)
  implicit val jsonWFormat: OFormat[Pagination] = Json.format[Pagination]

  def withOffsetAndLimit(offset : Int, limit : Int): Pagination = {
    val pageItemsOffset = ((offset % limit) - limit) % limit
    val pageNumber = (offset - pageItemsOffset) / limit + 1
    Pagination(pageNumber, pageSize = limit, pageItemsOffset)
  }
}
