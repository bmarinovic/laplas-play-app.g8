package core.api

import play.api.mvc.QueryStringBindable

object QueryStringBinders
{
  implicit def listBinder(implicit stringBinder: QueryStringBindable[String]) =
  {
    new QueryStringBindable[List[String]]
    {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, List[String]]] =
        stringBinder.bind(key, params).map(_.right.map(_.split(COMMA).toList))

      override def unbind(key: String, strings: List[String]): String =
        s"""\$key=\${strings.mkString(COMMA)}"""
    }
  }

  private val COMMA = ","
}
