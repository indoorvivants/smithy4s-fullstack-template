package hellosmithy4s

import cats.effect.*

trait Database:
  def stream[I, O](query: SqlOp[I, O]): fs2.Stream[IO, O]

  def vector[I, O](query: SqlOp[I, O]): IO[Vector[O]] =
    stream(query).compile.toVector

  def option[I, O](query: SqlOp[I, O]): IO[Option[O]] =
    vector(query).map(_.headOption)
