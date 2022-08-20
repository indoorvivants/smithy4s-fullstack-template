package hellosmithy4s

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.io.*
import java.nio.file.Paths

object Static:
  def routes =
    val indexHtml = StaticFile
      .fromResource[IO](
        "index.html",
        None,
        preferGzipped = true
      )
      .getOrElseF(NotFound())

    Resource.eval(indexHtml.memoize).map { idxHtml =>
      HttpRoutes.of[IO] {
        case req @ GET -> Root / "assets" / filename
            if filename.endsWith(".js") || filename.endsWith(".js.map") =>
          StaticFile
            .fromResource[IO](
              Paths.get("assets", filename).toString,
              Some(req),
              preferGzipped = true
            )
            .getOrElseF(NotFound())
        case req @ GET -> Root        => idxHtml
        case req if req.method == GET => idxHtml

      }
    }
  end routes
end Static
