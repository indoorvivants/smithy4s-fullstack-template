package hellosmithy4s

import cats.effect.*
import hellosmithy4s.spec.*
import skunk.*
import fs2.Stream
import skunk.data.Completion
import skunk.net.message.CommandComplete

trait Database:
  def getKey(key: Key): IO[Option[Value]]
  def create(key: Key, value: Option[Value]): IO[Unit]
  def getAll(): IO[Stream[IO, Pair]]
  def delete(key: Key): IO[Int]
  def inc(key: Key): IO[Pair]
  def dec(key: Key): IO[Pair]
  def update(key: Key, value: Value): IO[Pair]

object Database:

  def fromSessionPool(pool: SkunkSessionPool): Database =
    new Database:
      override def update(key: Key, value: Value): IO[Pair] = pool.use{s =>
        s.prepare(operations.update).flatMap(
          _.unique(Pair(key, value))
        )
      }

      override def dec(key: Key): IO[Pair] = pool.use{
        s => s.prepare(operations.decrementValue).flatMap(
          _.unique(key)
        )
      }

      override def inc(key: Key): IO[Pair] = pool.use{
        s => s.prepare(operations.incrementValue).flatMap(
          _.unique(key)
        )
      }

      override def delete(key: Key): IO[Int] = pool.use{
        s => s.prepare(operations.delete).flatMap{ps =>
            ps.execute(key).map{
              case Completion.Delete(n) => n
              case _ => throw KeyNotFound()
            }
          }
      }

      override def getAll() = pool.use{
        _.prepare(operations.getAll).map{
          _.stream(Void, 1024)
        }
      }

      override def create(key: Key, value: Option[Value]): IO[Unit] = pool.use{
        session =>
          session.prepare(operations.create).flatMap{
            _.execute(key, value.getOrElse(Value(0))).flatTap(IO.println).void
          }
      }

      override def getKey(key:Key): IO[Option[Value]] = pool.use{
        s => s.prepare(operations.getKey).flatMap(
          _.option(key)
        )
      }

    end new
  end fromSessionPool
end Database