package hellosmithy4s

import org.http4s.client.Client
import org.http4s.Uri
import cats.effect.IO

import spec.HelloService
import smithy4s.http4s.SimpleRestJsonBuilder
import org.http4s.dom.FetchClientBuilder
import org.scalajs.dom
import scala.concurrent.Future
import cats.effect.unsafe.implicits.global

case class Api(
    hello: HelloService[IO]
):
  import com.raquo.airstream.core.EventStream as LaminarStream
  import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*
  def future[A](a: Api => IO[A]): Future[A] =
    a(this).unsafeToFuture()

  def stream[A](a: Api => IO[A]): LaminarStream[A] =
    LaminarStream.fromFuture(future(a))
end Api

object Api:
  def create(location: String = dom.window.location.origin.get) =
    val uri = Uri.unsafeFromString(location)

    val client = FetchClientBuilder[IO].create

    val hello =
      SimpleRestJsonBuilder(HelloService)
        .client(client, uri)
        .fold(throw _, identity)

    Api(hello)
  end create
end Api
