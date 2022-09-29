package hellosmithy4s

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.io.*
import java.nio.file.Paths

object Static:
  def routes: Resource[IO, HttpRoutes[IO]] =
    val indexHtml = StaticFile
      .fromResource[IO](
        "assets/index.html",
        None,
        preferGzipped = true
      )
      .getOrElseF(NotFound())

    Resource.pure(HttpRoutes.of[IO] {
      case req @ GET -> Root / "assets" / filename
          if filename.endsWith(".js") || filename.endsWith(".js.map") =>
        println(getClass().getResource("assets/main.js"))
        StaticFile
          .fromResource[IO](
            Paths.get("assets", filename).toString,
            Some(req),
            preferGzipped = true
          )
          .getOrElseF(NotFound())
      case req @ GET -> Root        => indexHtml
      case req if req.method == GET => indexHtml
    })
  end routes
end Static
