package hellosmithy4s

case class PgCredentials(
    host: String,
    port: Int,
    user: String,
    database: String,
    password: Option[String],
    ssl: Boolean
):
  override def toString(): String =
    s"PgCredentials[host=$host:$port,user=$user,database=$database]"
end PgCredentials

object PgCredentials:
  def defaults(mp: Map[String, String]) =
    PgCredentials(
      host = mp.getOrElse("PG_HOST", "localhost"),
      port = mp.getOrElse("PG_PORT", "5432").toInt,
      user = mp.getOrElse("PG_USER", "postgres"),
      database = mp.getOrElse("PG_DB", "smithy4s_fullstack_template"),
      password = mp.get("PG_PASSWORD"),
      ssl = false
    )
end PgCredentials
