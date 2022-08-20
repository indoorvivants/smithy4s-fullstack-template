package hellosmithy4s

object HttpConfigLoader:
  import com.comcast.ip4s.*

  def bootstrap(
      cli: CLIConfig,
      env: Map[String, String] = Map.empty
  ) =
    HttpConfig(
      port = cli.port
        .flatMap(Port.fromInt)
        .orElse(env.get("PORT").flatMap(Port.fromString))
        .getOrElse(port"9000"),
      host = host"0.0.0.0",
      deployment = cli.deployment
    )
end HttpConfigLoader
