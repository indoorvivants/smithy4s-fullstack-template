package hellosmithy4s

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.io.*
import java.nio.file.Paths

object Static:
  val cls = getClass.getClassLoader()

  val routes: Resource[IO, HttpRoutes[IO]] =
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
        StaticFile
          .fromResource[IO](
            Paths.get("assets", filename).toString,
            Some(req),
            preferGzipped = true,
            classloader = Some(cls)
          )
          .getOrElseF(NotFound())
      case req @ GET -> Root        => indexHtml
      case req if req.method == GET => indexHtml
    })
  end routes
end Static
