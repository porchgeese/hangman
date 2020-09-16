package pt.porchgeese.shared

import pureconfig.ConfigReader
import cats.implicits._
import pureconfig.ConfigReader
import init.{InitError, MissingConfigKey}
object InitHashMap {
  def default[V](value: V) = InitHashMap[V](Map("default" -> value))
  implicit def reader[V](implicit configReader: ConfigReader[V]): ConfigReader[InitHashMap[V]] =
    ConfigReader[Map[String, V]].map(x => InitHashMap[V](x))
}
case class InitHashMap[V](private val values: Map[String, V]) {
  def all: Map[String, V]              = values
  def getDefault: Either[InitError, V] = get("default")
  def get(s: String): Either[InitError, V] =
    Either
      .fromOption[InitError, V](
        values.get(s),
        MissingConfigKey(s)
      )
}
