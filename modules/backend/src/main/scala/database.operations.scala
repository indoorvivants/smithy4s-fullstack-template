package hellosmithy4s

import cats.effect.IO

import spec.*

import cats.*, cats.data.*, cats.implicits.*
import cats.effect._
import skunk._
import skunk.implicits._
import skunk.codec.all._
import java.time.OffsetDateTime
import natchez.Trace.Implicits.noop
import smithy4s.Newtype

// sealed trait SqlOp[I, O]

// sealed abstract class SqlQuery[I, O](val input: I, val out: Query0[O])
//     extends SqlOp[I, O]
// sealed abstract class SqlUpdate[I](val input: I, val out: Update0)
//     extends SqlOp[I, Int]

// private def ntGet[T: Get](nt: Newtype[T]): Get[nt.Type] =
//   Get[T].map(nt.apply)

// private def ntPut[T: Put](nt: Newtype[T]): Put[nt.Type] =
//   Put[T].contramap[nt.Type](_.value)

// given Get[Key]   = ntGet(Key)
// given Get[Value] = ntGet(Value)

// given Put[Key]   = ntPut(Key)
// given Put[Value] = ntPut(Value)

object operations:

  val getAll: Query[Void, Pair] = sql"select key, value from examples order by key".query(codecs.userId ~ codecs.value).map(Pair.apply)

  val getKey: Query[Key, Value] = sql"select value from examples where key = ${codecs.userId}".query[Value](codecs.value)

  val create: Command[(Key, Value)] = sql"insert into examples (key, value) values (${codecs.userId}, ${codecs.value})".command

  val update: Query[Pair, Pair] = sql"update examples set value = ${codecs.value} where key = ${codecs.userId} RETURNING *"
    .query(codecs.value ~ codecs.userId)
    .contramap[Pair](p => p.value ~ p.key)
    .map{case(v ~ k) => Pair(k, v)}

  val delete: Query[Key, Int] = sql"delete from examples where key = ${codecs.userId} returning key".query(int4)

  val incrementValue: Query[Key, Pair] = sql"update examples set value = value + 1 where key = ${codecs.userId}".query(codecs.userId ~ codecs.value).map(Pair.apply)

  val decrementValue: Query[Key, Pair] = sql"update examples set value = value - 1 where key = ${codecs.userId}".query(codecs.userId ~ codecs.value).map(Pair.apply)

  // case class Get(key: Key)
  //     extends SqlQuery(
  //       key,
  //       sql"select value from examples where key = $key"
  //         .query[Int]
  //         .map(Value.apply)
  //     )

  // case object GetAll
  //     extends SqlQuery(
  //       (),
  //       sql"select key, value from examples order by key"
  //         .query[(Key, Value)]
  //         .map(Pair.apply)
  //     )

  // case class Create(key: Key, value: Option[Value])
  //     extends SqlUpdate(
  //       key,
  //       sql"insert into examples (key, value) values ($key, ${value.map(_.value).getOrElse(0)})".update
  //     )

  // case class Delete(key: Key)
  //     extends SqlUpdate(
  //       key,
  //       sql"delete from examples where key = $key".update
  //     )

  // case class Inc(key: Key)
  //     extends SqlUpdate(
  //       key,
  //       sql"update examples set value = value + 1 where key = $key".update
  //     )

  // case class Dec(key: Key)
  //     extends SqlUpdate(
  //       key,
  //       sql"update examples set value = value - 1 where key = $key".update
  //     )

  // case class Update(key: Key, value: Value)
  //     extends SqlUpdate(
  //       key,
  //       sql"update examples set value = ${value.value} where key = $key".update
  //     )
end operations
