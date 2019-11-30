package core.api

import play.api.libs.json._

case class ResultPage[T]
(
  pagination   : Pagination,
  totalRecords : Long,
  records      : Seq[T]
)
{
  def totalPagesCount: Long = (totalRecords + pagination.pageSize - 1) / pagination.pageSize
}

object ResultPage
{
  implicit def jsonWrites[T](implicit tWrites: Writes[T]): Writes[ResultPage[T]] = (resultPage: ResultPage[T]) => JsObject(Seq(
    "pagination" -> Json.toJson(resultPage.pagination),
    "totalRecords" -> JsNumber(resultPage.totalRecords),
    "totalPages" -> JsNumber(resultPage.totalPagesCount),
    "records" -> JsArray(resultPage.records.map(record ⇒ Json.toJson(record)))
  ))

  def empty[T](pagination: Pagination) : ResultPage[T] = {
    ResultPage[T](pagination = pagination, totalRecords = 0, Seq())
  }
}
