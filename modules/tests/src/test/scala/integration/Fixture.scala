package hellosmithy4s
package tests
package integration

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import cats.effect.kernel.Resource
import org.flywaydb.core.Flyway
import hellosmithy4s.PgCredentials
import hellosmithy4s.DoobieDatabase
import hellosmithy4s.Routes

import com.comcast.ip4s.*
import org.http4s.server

import cats.syntax.all.*
import scribe.Logger
import scribe.Level

def buildApp: Resource[IO, (Probe, server.Server)] =
  for
    _  <- silenceOfTheLogs
    db <- doobieDatabase
    logger = scribe.cats.io
    probe  <- Probe.build(logger, db)
    routes <- Routes.build(probe.api)
    httpConfig = HttpConfig(host"localhost", port"0", Deployment.Local)
    server <- Server(httpConfig, routes)
  yield probe -> server

def silenceOfTheLogs =
  val loggers =
    Seq(
      "org.http4s",
      "org.flywaydb.core",
      "org.testcontainers",
      "🐳 [postgres:14]",
      "🐳 [testcontainers/ryuk:0.3.3]"
    )

  val silence = loggers.traverse_ { log =>
    IO(Logger(log).withMinimumLevel(Level.Error).replace())
  }
  val shout = loggers.traverse_ { log =>
    IO(Logger(log).withMinimumLevel(Level.Info).replace())
  }

  Resource.make(silence)(_ => shout)
end silenceOfTheLogs

def doobieDatabase =
  postgresContainer
    .evalMap(cont => parseJDBC(cont.jdbcUrl).map(cont -> _))
    .evalTap { case (cont, _) =>
      migrate(cont.jdbcUrl, cont.username, cont.password)
    }
    .flatMap { case (cont, jdbcUrl) =>
      val pgConfig = PgCredentials.apply(
        host = jdbcUrl.getHost,
        port = jdbcUrl.getPort,
        user = cont.username,
        password = Some(cont.password),
        database = cont.databaseName
      )

      DoobieDatabase.hikari(pgConfig)
    }

private def migrate(url: String, user: String, password: String) =
  IO(Flyway.configure().dataSource(url, user, password).load()).flatMap { f =>
    IO(f.migrate())
  }

private def parseJDBC(url: String) = IO(java.net.URI.create(url.substring(5)))

private def postgresContainer =
  val start = IO(
    PostgreSQLContainer(dockerImageNameOverride =
      DockerImageName("postgres:14")
    )
  ).flatTap(cont => IO(cont.start()))

  Resource.make(start)(cont => IO(cont.stop()))
