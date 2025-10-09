package hellosmithy4s

object FlyioBootstrap:
  def pgCredentials(env: Map[String, String]): Option[PgCredentials] =
    env.get("DATABASE_URL").map { url =>

      val parsed = new java.net.URI(url)

      val host     = parsed.getHost()
      val port     = parsed.getPort()
      val userInfo = parsed.getUserInfo()
      val dbName   = parsed.getPath().tail // dropping the first slash

      val userName = userInfo.split(":").apply(0)
      val password = userInfo.split(":").apply(1)

      val ssl = false

      PgCredentials(host, port, userName, dbName, Some(password), ssl = ssl)
    }
end FlyioBootstrap
