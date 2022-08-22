package hellosmithy4s
package tests

import cats.effect.IO
import cats.effect.kernel.Resource
import hellosmithy4s.Database
import hellosmithy4s.Services
import hellosmithy4s.spec.HelloService
import scribe.Scribe
import weaver.*

case class Probe(api: Services)

object Probe:
  def build(logger: Scribe[IO], database: Database) =
    Resource.eval(IO(Probe(Services.build(logger, database))))

trait BaseSuite extends IOSuite:
  override type Res = Probe

  def probeTest(name: weaver.TestName)(f: Probe => IO[weaver.Expectations]) =
    test(name) { (probe, log) =>
      f(probe).attempt.flatMap(IO.fromEither)
    }
end BaseSuite
