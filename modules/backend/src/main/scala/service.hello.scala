package hellosmithy4s

import cats.effect.*
import scribe.Scribe
import cats.syntax.all.*

import hellosmithy4s.spec.*

class HelloImplementation(logger: Scribe[IO], db: Database)
    extends HelloService[IO]:

  override def get(key: Key): IO[GetOutput] =
    db.getKey(key).flatMap {
      case Some(value) => GetOutput(value).pure[IO]
      case None        => IO.raiseError(KeyNotFound())
    }

  override def dec(key: Key): IO[Unit] =
    orNotFound(db.dec(key).map(_.map(_ => 1))).void

  override def inc(key: Key): IO[Unit] =
    orNotFound(db.inc(key).map(_.map(_ => 1))).void

  override def update(key: Key, value: Value): IO[Unit] = orNotFound(
    db.update(key, value).map(opt => opt.map(_ => 1))
  ).void

  override def create(key: Key, value: Option[Value]): IO[Unit] =
    db.create(key, value).attempt.flatMap {
      case Left(err) =>
        logger.error(
          s"Failed to insert key ${key.value}",
          err
        ) *> IO.raiseError(KeyAlreadyExists())
      case _ => logger.info(s"Key ${key.value} was added")
    }

  override def delete(key: Key): IO[Unit] = orNotFound(
    db.delete(key).map(_.some)
  ).void

  override def getAll(): IO[GetAllOutput] =
    db.getAll().flatMap(str => str.compile.toList.map(GetAllOutput(_)))

  private def orNotFound(result: IO[Option[Int]]) =
    result.flatMap {
      case None | Some(0) => IO.raiseError(KeyNotFound())
      case other          => other.pure[IO]
    }
end HelloImplementation
