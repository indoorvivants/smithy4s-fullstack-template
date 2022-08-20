package hellosmithy4s

import cats.data.Kleisli
import cats.effect.*
import cats.syntax.all.*
import hellosmithy4s.spec.HelloService
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.implicits.*
import scribe.Scribe
import smithy4s.http4s.SimpleProtocolBuilder
import smithy4s.http4s.SimpleRestJsonBuilder
import org.http4s.Response

type ErrorLogger = (Request[IO], Throwable) => IO[Unit]
object ErrorLogger:
  val void: ErrorLogger = (_, _) => IO.unit

object Routes:
  def build(
      app: Services,
      errorLogger: ErrorLogger = ErrorLogger.void
  ) =
    def handleErrors(routes: HttpRoutes[IO]) =
      routes.orNotFound.onError { exc =>
        Kleisli(request => errorLogger(request, exc))
      }

    val serviceRoutes = SimpleRestJsonBuilder.routes(app.hello).resource
    val staticRoutes = Static.routes

    (serviceRoutes, staticRoutes)
      .parMapN(_ <+> _)
      .map(handleErrors)
  end build
end Routes
