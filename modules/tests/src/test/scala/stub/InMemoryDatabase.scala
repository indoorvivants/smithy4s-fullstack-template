package hellosmithy4s
package tests
package stub

import cats.effect.*

import operations.*
import hellosmithy4s.spec.*
import fs2.Stream

class InMemoryDatabase private (rf: Ref[IO, Map[Key, Value]]) extends Database:

  override def update(key: Key, value: Value): IO[Pair] = rf.modify { mp =>
    if mp.contains(key) then
      val newMap = mp.updatedWith(key)(_.map(v => value))
      (newMap, Pair(key, newMap(key)))
    else throw KeyNotFound()
  }

  override def dec(key: Key): IO[Pair] = rf.modify[Pair] { mp =>
    if mp.contains(key) then
      val newMap = mp.updatedWith(key)(_.map(v => Value(v.value - 1)))
      (newMap, Pair(key, newMap(key)))
    else throw KeyNotFound()
  }

  override def inc(key: Key): IO[Pair] = rf.modify[Pair] { mp =>
    if mp.contains(key) then
      println("here")
      val newMap = mp.updatedWith(key)(_.map(v => Value(v.value + 1)))
      (newMap, Pair(key, newMap(key)))
    else
      println("Throwing error")
      throw KeyNotFound()
  }

  override def delete(key: Key): IO[Int] = rf.modify { mp =>
    if mp.contains(key) then mp.removed(key) -> IO.pure(1)
    else mp                                  -> IO.raiseError(KeyNotFound())
  }.flatten

  override def getAll(): IO[Stream[IO, Pair]] =
    rf.get.map(ma => fs2.Stream.emits(ma.toList.map((k, v) => Pair(k, v))))

  override def create(key: Key, value: Option[Value]): IO[Unit] =
    rf.modify { mp =>
      if mp.contains(key) then throw KeyAlreadyExists()
      else (mp.updated(key, value.getOrElse(Value(0))), ())
    }

  override def getKey(key: Key): IO[Option[Value]] =
    rf.get.map(ma => ma.get(key))

  // override def stream[I, O](query: SqlOp[I, O]): fs2.Stream[cats.effect.IO, O] =
  //   query match
  //     case Get(key) =>
  //       fs2.Stream.evalSeq(rf.get.map(_.get(Key(key.value)).toSeq))
  //     case GetAll =>
  //       fs2.Stream.evalSeq(
  //         rf.get.map(_.toSeq.map((k, v) => Pair(k, v)))
  //       )
  //     case Create(key, value) =>
  //       fs2.Stream
  //         .eval(rf.modify { mp =>
  //           if mp.contains(key) then mp -> IO.raiseError(KeyAlreadyExists())
  //           else mp.updated(key, value.getOrElse(Value(0))) -> IO.pure(1)
  //         })
  //         .evalMap(identity)
  //     case Delete(key) =>
  //       fs2.Stream
  //         .eval(rf.modify { mp =>
  //           if mp.contains(key) then mp.removed(key) -> IO.pure(1)
  //           else mp -> IO.raiseError(KeyNotFound())
  //         })
  //         .evalMap(identity)

  //     case Inc(key) =>
  //       fs2.Stream
  //         .eval(rf.modify { mp =>
  //           if mp.contains(key) then
  //             mp.updatedWith(key)(_.map(v => Value(v.value + 1))) -> IO.pure(1)
  //           else mp -> IO.raiseError(KeyNotFound())
  //         })
  //         .evalMap(identity)

  //     case Dec(key) =>
  //       fs2.Stream
  //         .eval(rf.modify { mp =>
  //           if mp.contains(key) then
  //             mp.updatedWith(key)(_.map(v => Value(v.value - 1))) -> IO.pure(1)
  //           else mp -> IO.raiseError(KeyNotFound())
  //         })
  //         .evalMap(identity)

  //     case Update(key, value) =>
  //       fs2.Stream
  //         .eval(rf.modify { mp =>
  //           if mp.contains(key) then mp.updated(key, value) -> IO.pure(1)
  //           else mp -> IO.raiseError(KeyNotFound())
  //         })
  //         .evalMap(identity)

end InMemoryDatabase

object InMemoryDatabase:
  def create = IO.ref(Map.empty).map(InMemoryDatabase(_))
