package hellosmithy4s

import org.http4s.client.Client
import org.http4s.Uri
import cats.effect.IO

import spec.HelloService

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.Thenable.Implicits.*
import scala.util.Failure
import scala.util.Success
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import smithy4s_fetch.SimpleRestJsonFetchClient

def api(using a: Api): Api = a

class Api private (
    val hello: HelloService[Promise]
):
  import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*
  def future[A](a: Api => Promise[A]): Future[A] =
    a(this)

  def futureAttempt[A](a: Api => Promise[A]): Future[Either[Throwable, A]] =
    a(this).transform:
      case Success(value)     => Success(Right(value))
      case Failure(exception) => Success(Left(exception))

  def stream[A](a: Api => Promise[A]): EventStream[A] =
    EventStream.fromJsPromise(a(this))

  def signal[A](a: Api => Promise[A]): Signal[Option[A]] =
    Signal.fromJsPromise(a(this))
end Api

object Api:
  def create(location: String = org.scalajs.dom.window.location.origin) =

    val client =
      SimpleRestJsonFetchClient(HelloService, location).make

    Api(client)

  end create
end Api
