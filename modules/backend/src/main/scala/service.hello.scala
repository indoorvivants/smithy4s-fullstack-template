package hellosmithy4s

import cats.effect.*
import scribe.Scribe
import cats.syntax.all.*

import hellosmithy4s.spec.*

class HelloImplementation(logger: Scribe[IO], database: Database)
    extends HelloService[IO]:
  override def get(key: Key): IO[GetOutput] =
    database.option(operations.Get(key)).flatMap {
      case Some(value) => GetOutput(Value(value)).pure[IO]
      case None        => IO.raiseError(KeyNotFound())
    }

  override def dec(key: Key): IO[Unit] =
    orNotFound(database.option(operations.Dec(key))).void

  override def inc(key: Key): IO[Unit] =
    orNotFound(database.option(operations.Inc(key))).void

  override def update(key: Key, value: Value): IO[Unit] =
    orNotFound(database.option(operations.Update(key, value))).void

  override def create(key: Key, value: Option[Value]): IO[Unit] =
    database
      .option(operations.Create(key, value))
      .attempt
      .flatMap {
        case Left(err) =>
          logger.error(
            s"Failed to insert key ${key.value}",
            err
          ) *> IO.raiseError(KeyAlreadyExists())
        case _ => logger.info(s"Key ${key.value} was added")
      }

  override def delete(key: Key): IO[Unit] =
    orNotFound(database.option(operations.Delete(key))).void

  override def getAll(): IO[GetAllOutput] =
    database.vector(operations.GetAll).map { v =>
      GetAllOutput(v.toList)
    }

  private def orNotFound(result: IO[Option[Int]]) =
    result.flatMap {
      case None | Some(0) => IO.raiseError(KeyNotFound())
      case other          => other.pure[IO]
    }
end HelloImplementation
