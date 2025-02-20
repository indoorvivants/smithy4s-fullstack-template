package hellosmithy4s

import cats.effect.IOApp
import cats.syntax.all.*
import natchez.Trace.Implicits.noop

object Run extends IOApp:
  def run(args: List[String]) =
    bootstrap(args, sys.env).useForever
