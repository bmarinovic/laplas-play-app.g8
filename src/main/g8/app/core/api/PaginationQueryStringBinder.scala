package core.api

import play.api.mvc.QueryStringBindable

object PaginationQueryStringBinder
{
  implicit def paginationBinder(implicit stringBinder: QueryStringBindable[Int]) : QueryStringBindable[Pagination] =
  {
    new QueryStringBindable[Pagination]
    {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Pagination]] = {
        val pageNumberBinding = stringBinder.bind("pageNumber", params)
        val pageSizeBinding   = stringBinder.bind("pageSize", params)

        (pageNumberBinding, pageSizeBinding) match
        {
          case (Some(_), None) =>
            Some(Left("pageSize is missing."))
          case (Some(Right(pageNumber)), _) if pageNumber <= 0 =>
            Some(Left("pageNumber needs to be larger than 0."))
          case (None, Some(_)) =>
            Some(Left("pageNumber is missing."))
          case (_, Some(Right(pageSize))) if pageSize <= 0 =>
            Some(Left("pageSize needs to be larger than 0."))
          case (None, None) =>
            None
          case (Some(pageNumberEither), Some(pageSizeEither)) =>
            Some(validate(pageNumberEither, pageSizeEither))
        }
      }

      private def validate(pageNumberEither: Either[String, Int], pageSizeEither: Either[String, Int]) : Either[String, Pagination] =
        (pageNumberEither, pageSizeEither) match
        {
          case (Right(pageNumberValue), Right(pageSizeValue)) => Right(Pagination(pageNumberValue, pageSizeValue))
          case (Left(error), Right(_)) =>
            Left(error)
          case (Right(_), Left(error)) =>
            Left(error)
          case (Left(error1), Left(error2)) =>
            Left(s"\$error1, \$error2")
        }

      override def unbind(key: String, pagination : Pagination): String =
        s"\${stringBinder.unbind("pageNumber", pagination.pageNumber)}&\${stringBinder.unbind("pageSize", pagination.pageSize)}"
    }
  }
}
