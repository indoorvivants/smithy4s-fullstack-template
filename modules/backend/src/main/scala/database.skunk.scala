package hellosmithy4s

import cats.effect.*
import skunk.*
import skunk.data.Completion
import cats.syntax.all.*
import org.typelevel.otel4s.trace.Tracer

class SkunkDatabase private (makeSession: Resource[IO, Session[IO]])
    extends Database:

  private val defaultChunkSize = 512

  private val transactionalSession = for
    session     <- makeSession
    transaction <- session.transaction
  yield (session, transaction)

  private def transact[A](
      body: Session[IO] => fs2.Stream[IO, A]
  ): fs2.Stream[IO, A] =
    fs2.Stream.resource(transactionalSession).flatMap { (session, _) =>
      body(session)
    }

  private def transact[A](body: Session[IO] => IO[A]): IO[A] =
    transactionalSession.use { (session, _) =>
      body(session)
    }

  private def dmlAffectedCount(
      completion: Completion,
      command: Command[?]
  ): IO[Int] = completion match
    case Completion.Update(count) => count.pure[IO]
    case Completion.Insert(count) => count.pure[IO]
    case Completion.Delete(count) => count.pure[IO]
    case x =>
      IO.raiseError(
        new RuntimeException(
          s"Unexpected completion $x, SQL was ${command.sql}"
        )
      )

  override def stream[I, O](query: SqlOp[I, O]): fs2.Stream[IO, O] =
    query match
      case sq: SqlQuery[I, O] =>
        transact { session =>
          session.stream(sq.out, sq.input, defaultChunkSize)
        }
      case sq: SqlUpdate[?] =>
        fs2.Stream.eval(transact { session =>
          session
            .execute(sq.out)(sq.input)
            .flatMap(completion => dmlAffectedCount(completion, sq.out))
        })
end SkunkDatabase

object SkunkDatabase:
  def make(creds: PgCredentials, config: SkunkConfig)(using Tracer[IO]) =
    Session
      .pooled[IO](
        host = creds.host,
        port = creds.port,
        user = creds.user,
        password = creds.password,
        database = creds.database,
        // TODO double-check if this is equivalent with the hikari setup
        max = config.maxSessions
      )
      .map(SkunkDatabase(_))
end SkunkDatabase

case class SkunkConfig(
    maxSessions: Int,
    strategy: skunk.TypingStrategy,
    debug: Boolean
)

object SkunkConfig
    extends SkunkConfig(
      maxSessions = 2,
      strategy = skunk.TypingStrategy.SearchPath,
      debug = false
    )

// import doobie.*, doobie.implicits.*, doobie.hikari.*
// import cats.effect.IO

// class DoobieDatabase private (transactor: Transactor[IO]) extends Database:
//   override def stream[I, O](query: SqlOp[I, O]): fs2.Stream[cats.effect.IO, O] =
//     query match
//       case sq: SqlQuery[I, O] => sq.out.stream.transact(transactor)
//       case sq: SqlUpdate[?] => fs2.Stream.eval(sq.out.run.transact(transactor))
// end DoobieDatabase

// object DoobieDatabase:
//   def hikari(creds: PgCredentials) =
//     ExecutionContexts
//       .fixedThreadPool[IO](32)
//       .flatMap { ec =>
//         HikariTransactor.newHikariTransactor(
//           "org.postgresql.Driver", // driver classname
//           s"jdbc:postgresql://${creds.host}:${creds.port}/${creds.database}", // connect URL (driver-specific)
//           s"${creds.user}",             // user
//           creds.password.getOrElse(""), // password
//           ec
//         )
//       }
//       .map(new DoobieDatabase(_))
// end DoobieDatabase
