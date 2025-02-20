package hellosmithy4s

import cats.effect.IO

import spec.*

import cats.*, cats.data.*, cats.implicits.*
import skunk.{Codec, Query}
import skunk.implicits.*
import skunk.codec.all.*

import smithy4s.Newtype
import skunk.data.Completion
import skunk.Command

sealed trait SqlOp[I, O]

sealed abstract class SqlQuery[I, O](val input: I, val out: Query[I, O])
    extends SqlOp[I, O]

sealed abstract class SqlUpdate[I](val input: I, val out: Command[I])
    extends SqlOp[I, Int]

def newtypeCodec[A](nt: Newtype[A], underlying: Codec[A]): Codec[nt.Type] =
  underlying.imap(nt.apply)(_.value)
val keyCodec   = newtypeCodec(Key, varchar(50))
val valueCodec = newtypeCodec(Value, int4)
val pairCodec  = (keyCodec *: valueCodec).to[Pair]

object operations:

  case class Get(key: Key)
      extends SqlQuery(
        key,
        sql"select value from examples where key = ${newtypeCodec(Key, varchar(50))}"
          .query(valueCodec)
      )

  case object GetAll
      extends SqlQuery(
        skunk.Void,
        sql"select key, value from examples order by key"
          .query(pairCodec)
      )

  case class Create(key: Key, value: Value)
      extends SqlUpdate(
        (key, value),
        sql"insert into examples (key, value) values ($keyCodec, $valueCodec)".command
      )

  case class Delete(key: Key)
      extends SqlUpdate(
        key,
        sql"delete from examples where key = $keyCodec".command
      )

  case class Inc(key: Key)
      extends SqlUpdate(
        key,
        sql"update examples set value = value + 1 where key = $keyCodec".command
      )

  case class Dec(key: Key)
      extends SqlUpdate(
        key,
        sql"update examples set value = value - 1 where key = $keyCodec".command
      )

  case class Update(key: Key, value: Value)
      extends SqlUpdate(
        (key, value),
        sql"update examples set value = ${valueCodec} where key = $keyCodec".command
          .contramap(_.swap)
      )
end operations
