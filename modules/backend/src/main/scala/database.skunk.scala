package hellosmithy4s

import cats.*, cats.effect.*, cats.implicits.*
import skunk._
import skunk.implicits._
import skunk.codec.all._
import natchez.Trace.Implicits.noop
import cats.effect.IO

type SkunkSessionPool = Resource[IO, Session[IO]]

object SkunkDatabase:
  def sessionPool(creds: PgCredentials) : Resource[IO, SkunkSessionPool] =
    Session.pooled(
      host     = creds.host,
      port     = creds.port,
      user     = creds.user,
      database = creds.database,
      password = creds.password,
      max = 32
    )
end SkunkDatabase
