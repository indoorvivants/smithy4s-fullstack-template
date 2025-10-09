package hellosmithy4s.tests.playwright

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.io.*
import java.nio.file.Paths
import java.io.File

object Static:

  def routes(base: File): Resource[IO, HttpRoutes[IO]] =
    val fPath = fs2.io.file.Path.fromNioPath(base.toPath.toAbsolutePath)
    val indexHtml = StaticFile
      .fromPath[IO](
        fPath.resolve("index.html")
      )
      .getOrElseF(NotFound())

    Resource
      .pure(HttpRoutes.of[IO] {
        case req @ GET -> Root / "assets" / filename
            if filename.endsWith(".js") || filename.endsWith(".js.map") =>
          StaticFile
            .fromPath[IO](
              fPath.resolve("assets").resolve(filename),
              Some(req)
            )
            .getOrElseF(NotFound())
        case req @ GET -> Root        => indexHtml
        case req if req.method == GET => indexHtml
      })
  end routes
end Static
