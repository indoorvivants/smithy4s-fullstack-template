package hellosmithy4s

import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import com.comcast.ip4s.Literals.host

enum Deployment:
  case Live, Local

case class HttpConfig(host: Host, port: Port, deployment: Deployment)
