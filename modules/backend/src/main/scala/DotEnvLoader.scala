package hellosmithy4s

import cats.effect.IO
import java.io.File
import java.io.FileInputStream

import scala.jdk.CollectionConverters.*

object DotEnvLoader:
  def load(file: File): IO[Map[String, String]] =
    for
      f  <- IO(file)
      is <- IO(new FileInputStream(f))
      props = new java.util.Properties()
      loaded <-
        IO(props.load(is)).guarantee(IO(is.close()))
    yield props.entrySet.asScala.map { e =>
      e.getKey.asInstanceOf[String] -> e.getValue.asInstanceOf[String]
    }.toMap
end DotEnvLoader
