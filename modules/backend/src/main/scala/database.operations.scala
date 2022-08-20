package hellosmithy4s

import cats.effect.IO

import spec.*

import cats.*, cats.data.*, cats.implicits.*
import doobie.*, doobie.implicits.*

sealed trait SqlOp[I, O]

sealed abstract class SqlQuery[I, O](val input: I, val out: Query0[O])
    extends SqlOp[I, O]
sealed abstract class SqlUpdate[I](val input: I, val out: Update0)
    extends SqlOp[I, Int]

object operations:
  case class Get(key: Key)
      extends SqlQuery(
        key,
        sql"select value from examples where key = ${key.value}".query[Int]
      )

  case class Create(key: Key)
      extends SqlUpdate(
        key,
        sql"insert into examples (key, value) values (${key.value}, 0)".update
      )

  case class Delete(key: Key)
      extends SqlUpdate(
        key,
        sql"delete from examples where key = ${key.value}".update
      )

  case class Inc(key: Key)
      extends SqlUpdate(
        key,
        sql"update examples set value = value + 1 where key = ${key.value}".update
      )

  case class Dec(key: Key)
      extends SqlUpdate(
        key,
        sql"update examples set value = value - 1 where key = ${key.value}".update
      )

  case class Update(key: Key, value: Value)
      extends SqlUpdate(
        key,
        sql"update examples set value = ${value.value} where key = ${key.value}".update
      )
end operations
