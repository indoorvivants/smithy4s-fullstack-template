package hellosmithy4s

import cats.effect.*
import cats.syntax.all.*
import org.http4s.server.Server
import java.io.File

def bootstrap(
    arguments: List[String],
    systemEnv: Map[String, String]
): Resource[IO, Server] =
  val logger = scribe.cats.io
  val cliConfig =
    CLIConfig(None, Option(new File("env.opts")), Deployment.Local, Cloud.Flyio)

  val opts = cliConfig.optsFile match
    case None => IO(Map.empty)
    case Some(file) =>
      loadProps(file).handleErrorWith { err =>
        logger
          .error(
            s"Failed to load properties from file ${file.getAbsolutePath}",
            err
          )
          .as(Map.empty)
      }

  val allEnv = opts.map { fallback =>
    (systemEnv.keySet ++ fallback.keySet).flatMap { key =>
      val value = systemEnv.get(key).orElse(fallback.get(key))

      value.map(v => key -> v)
    }.toMap
  }

  for
    env <- Resource.eval(allEnv)

    httpConfig = HttpConfigLoader.bootstrap(
      cliConfig,
      env
    )
    cloudDb = cliConfig.cloud match
      case Cloud.Flyio => FlyioBootstrap.pgCredentials(env)

    pgCredentials = cloudDb.getOrElse(PgCredentials.from(env))

    services = Services.build(
      logger,
      DoobieDatabase.build(pgCredentials)
    )

    routes <- Routes.build(
      services,
      (req, exc) => logger.error(s"[HTTP REQUEST FAILED] $req", exc)
    )

    _ <- migrate(pgCredentials)

    server <- Server(httpConfig, routes)
  yield server
  end for
end bootstrap
