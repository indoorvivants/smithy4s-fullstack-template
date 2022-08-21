package hellosmithy4s

import java.io.File

case class CLIConfig(
    port: Option[Int],
    optsFile: Option[File],
    deployment: Deployment,
    cloud: Cloud
)

enum Cloud:
  case Flyio
