package hellosmithy4s

import org.flywaydb.core.Flyway
import cats.effect.*
import org.flywaydb.core.api.exception.FlywayValidateException

def migrate(postgres: PgCredentials): Resource[IO, Unit] =
  import postgres.*
  val url =
    s"jdbc:postgresql://$host:$port/$database"

  val flyway =
    IO(Flyway.configure().dataSource(url, user, password.getOrElse("")).load())
      .flatMap { f =>
        val migrate = IO(f.migrate()).void
        val repair  = IO(f.repair()).void

        migrate.handleErrorWith {
          case _: FlywayValidateException =>
            repair.redeemWith[Unit](
              ex => IO.raiseError(ex),
              _ => migrate
            )
          case other => IO.raiseError(other)
        }
      }

  Resource.eval(flyway)
end migrate
