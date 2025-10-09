package hellosmithy4s

import cats.effect.*
import org.typelevel.otel4s.trace.Tracer
import dumbo.Dumbo
import dumbo.ConnectionConfig

def migrate(postgres: PgCredentials)(using Tracer[IO]) =

  given dumbo.logging.Logger[IO] =
    case (dumbo.logging.LogLevel.Info, message) => Log.info(message)
    case (dumbo.logging.LogLevel.Warn, message) => Log.warn(message)

  Dumbo
    .withResourcesIn[IO]("db/migration")
    .apply(
      connection = ConnectionConfig(
        host = postgres.host,
        port = postgres.port,
        user = postgres.user,
        database = postgres.database,
        password = postgres.password,
        ssl =
          if postgres.ssl then ConnectionConfig.SSL.Trusted
          else ConnectionConfig.SSL.None
      ),
      defaultSchema = "public"
    )
    .runMigration
end migrate
