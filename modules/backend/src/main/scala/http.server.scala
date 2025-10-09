package hellosmithy4s

import org.http4s.ember.server.EmberServerBuilder
import cats.effect.IO
import org.http4s.HttpApp
import scala.concurrent.duration.*

def Server(config: HttpConfig, app: HttpApp[IO]) =
  EmberServerBuilder
    .default[IO]
    .withPort(config.port)
    .withHost(config.host)
    .withShutdownTimeout(1.second)
    .withHttpApp(app)
    .build
    .evalTap: server =>
      Log.info(s"Server started on ${server.baseUri}")
end Server
