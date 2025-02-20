package hellosmithy4s
package tests
package stub

import cats.effect.*

import operations.*
import hellosmithy4s.spec.*

class InMemoryDatabase private (rf: Ref[IO, Map[Key, Value]]) extends Database:
  override def stream[I, O](query: SqlOp[I, O]): fs2.Stream[cats.effect.IO, O] =
    query match
      case Get(key) =>
        fs2.Stream.evalSeq(rf.get.map(_.get(Key(key.value)).toSeq))
      case GetAll =>
        fs2.Stream.evalSeq(
          rf.get.map(_.toSeq.map((k, v) => Pair(k, v)))
        )
      case Create(key, value) =>
        fs2.Stream
          .eval(rf.modify { mp =>
            if mp.contains(key) then mp -> IO.raiseError(KeyAlreadyExists())
            else mp.updated(key, value) -> IO.pure(1)
          })
          .evalMap(identity)
      case Delete(key) =>
        fs2.Stream
          .eval(rf.modify { mp =>
            if mp.contains(key) then mp.removed(key) -> IO.pure(1)
            else mp -> IO.raiseError(KeyNotFound())
          })
          .evalMap(identity)

      case Inc(key) =>
        fs2.Stream
          .eval(rf.modify { mp =>
            if mp.contains(key) then
              mp.updatedWith(key)(_.map(v => Value(v.value + 1))) -> IO.pure(1)
            else mp -> IO.raiseError(KeyNotFound())
          })
          .evalMap(identity)

      case Dec(key) =>
        fs2.Stream
          .eval(rf.modify { mp =>
            if mp.contains(key) then
              mp.updatedWith(key)(_.map(v => Value(v.value - 1))) -> IO.pure(1)
            else mp -> IO.raiseError(KeyNotFound())
          })
          .evalMap(identity)

      case Update(key, value) =>
        fs2.Stream
          .eval(rf.modify { mp =>
            if mp.contains(key) then mp.updated(key, value) -> IO.pure(1)
            else mp -> IO.raiseError(KeyNotFound())
          })
          .evalMap(identity)

end InMemoryDatabase

object InMemoryDatabase:
  def create = IO.ref(Map.empty).map(InMemoryDatabase(_))
