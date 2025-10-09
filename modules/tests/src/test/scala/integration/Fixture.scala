package hellosmithy4s
package tests
package integration

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import hellosmithy4s.PgCredentials
import hellosmithy4s.Routes
import hellosmithy4s.SkunkDatabase
import org.http4s.server
import org.testcontainers.utility.DockerImageName
import org.typelevel.otel4s.trace.Tracer
import scribe.Level
import scribe.Logger
import java.io.File
import hellosmithy4s.tests.playwright.Static

def buildApp(
    frontendDist: Option[File] = None,
    silenceLogs: Boolean = true
): Resource[IO, (Probe, server.Server)] =
  for
    _  <- if silenceLogs then silenceOfTheLogs else Resource.eval(IO.unit)
    db <- skunkDatabase
    logger = scribe.cats.io
    probe <- Probe.build(logger, db)
    staticRoutes <- frontendDist
      .map(Static.routes(_))
      .getOrElse(Resource.pure(org.http4s.HttpRoutes.empty[IO]))
    routes <- Routes.build(probe.api, staticRoutes)
    httpConfig = HttpConfig(host"localhost", port"0", Deployment.Local)
    server <- Server(httpConfig, routes)
  yield probe -> server

def silenceOfTheLogs =
  val loggers =
    Seq(
      "org.http4s",
      "org.testcontainers",
      "🐳 [postgres:17]",
      "🐳 [testcontainers/ryuk:0.3.3]",
      "com.zaxxer.hikari"
    )

  val silence = loggers.traverse_ { log =>
    IO(Logger(log).withMinimumLevel(Level.Error).replace())
  }
  val shout = loggers.traverse_ { log =>
    IO(Logger(log).withMinimumLevel(Level.Info).replace())
  }

  Resource.make(silence)(_ => shout)
end silenceOfTheLogs

def skunkDatabase =
  given Tracer[IO] = Tracer.Implicits.noop
  postgresContainer
    .evalMap(cont => parseJDBC(cont.jdbcUrl).map(cont -> _))
    .map: (cont, jdbcUrl) =>
      PgCredentials(
        host = jdbcUrl.getHost,
        port = jdbcUrl.getPort,
        user = cont.username,
        password = Some(cont.password),
        database = cont.databaseName,
        ssl = false
      )
    .evalTap(migrate(_))
    .flatMap: pgConfig =>
      SkunkDatabase.make(pgConfig, SkunkConfig)
end skunkDatabase

private def parseJDBC(url: String) = IO(java.net.URI.create(url.substring(5)))

private def postgresContainer =
  val start = IO(
    PostgreSQLContainer(dockerImageNameOverride =
      DockerImageName("postgres:17")
    )
  ).flatTap(cont => IO(cont.start()))

  Resource.make(start)(cont => IO(cont.stop()))
