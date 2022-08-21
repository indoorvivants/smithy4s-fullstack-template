package hellosmithy4s

import cats.*, cats.data.*, cats.implicits.*
import doobie.*, doobie.implicits.*
import cats.effect.IO

class DoobieDatabase(transactor: Transactor[IO]) extends Database:
  override def stream[I, O](query: SqlOp[I, O]): fs2.Stream[cats.effect.IO, O] =
    query match
      case sq: SqlQuery[I, O] => sq.out.stream.transact(transactor)
      case sq: SqlUpdate[?] => fs2.Stream.eval(sq.out.run.transact(transactor))
end DoobieDatabase

object DoobieDatabase:
  def build(creds: PgCredentials) =
    val ta = Transactor
      .fromDriverManager[IO](
        "org.postgresql.Driver", // driver classname
        s"jdbc:postgresql://${creds.host}:${creds.port}/${creds.database}", // connect URL (driver-specific)
        s"${creds.user}",            // user
        creds.password.getOrElse("") // password
      )
    DoobieDatabase(ta)
end DoobieDatabase
