package hellosmithy4s

case class PgCredentials(
    host: String,
    port: Int,
    user: String,
    database: String,
    password: Option[String],
    ssl: Boolean
)

object PgCredentials:
  def from(mp: Map[String, String]) =
    PgCredentials(
      host = mp.getOrElse("PG_HOST", "localhost"),
      port = mp.getOrElse("PG_PORT", "5432").toInt,
      user = mp.getOrElse("PG_USER", "postgres"),
      database = mp.getOrElse("PG_DB", "postgres"),
      password = mp.get("PG_PASSWORD"),
      ssl = false
    )
end PgCredentials
