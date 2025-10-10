package hellosmithy4s

import java.io.File
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port

case class CLIConfig(
    port: Option[Int],
    optsFile: Option[File],
    cloud: Option[Cloud]
)

case class HttpConfig(port: Port, host: Host)

enum Cloud:
  case Flyio
