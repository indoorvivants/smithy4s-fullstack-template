package hellosmithy4s

import cats.implicits.*
import skunk.{Session, Command}
import skunk.data.Completion
import fs2.Stream
import cats.effect.{IO, Resource}
import natchez.Trace

class SkunkDatabase private (makeSession: Resource[IO, Session[IO]])
    extends Database:

  private val defaultChunkSize = 512

  private val transactionalSession = for
    session     <- makeSession
    transaction <- session.transaction
  yield (session, transaction)

  private def transact[A](body: Session[IO] => Stream[IO, A]): Stream[IO, A] =
    Stream.resource(transactionalSession).flatMap { (session, _) =>
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
        Stream.eval(transact { session =>
          session
            .execute(sq.out)(sq.input)
            .flatMap(completion => dmlAffectedCount(completion, sq.out))
        })
end SkunkDatabase

object SkunkDatabase:
  def make(creds: PgCredentials)(using trace: Trace[IO]) =
    Session
      .pooled[IO](
        host = creds.host,
        port = creds.port,
        user = creds.user,
        password = creds.password,
        database = creds.database,
        // TODO double-check if this is equivalent with the hikari setup
        max = 32
      )
      .map(SkunkDatabase(_))
end SkunkDatabase
