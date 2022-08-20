package hellosmithy4s

import hellosmithy4s.spec.HelloService
import cats.effect.IO
import scribe.Scribe

case class Services(hello: HelloService[IO])

object Services:
  def build(logger: Scribe[IO], database: Database) =
    Services(HelloImplementation(logger, database))
