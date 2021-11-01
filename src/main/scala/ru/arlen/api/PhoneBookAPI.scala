package ru.arlen.api

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import ru.arlen.dto._
import ru.arlen.services.PhoneBookService
import zio.RIO
import zio.interop.catz._

class PhoneBookApi[R <: PhoneBookService.PhoneBookService] {

  type PhoneBookTask[A] = RIO[R, A]

  val dsl = Http4sDsl[PhoneBookTask]
  import dsl._

  implicit def jsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[PhoneBookTask, A] = jsonOf[PhoneBookTask, A]
  implicit def jsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[PhoneBookTask, A] = jsonEncoderOf[PhoneBookTask, A]

  def route = HttpRoutes.of[PhoneBookTask] {
    case GET -> Root / phone =>
      PhoneBookService
        .find(phone)
        .foldM(
          err => NotFound(),
          result => Ok(result)
        )
    case req @ POST -> Root =>
      (for {
        record <- req.as[PhoneRecord] // as сериализует body в case class
        result <- PhoneBookService.insert(record)
      } yield result)
        .foldM(err => BadRequest(err.getMessage()), result => Ok(result))
    case req @ PUT -> Root / id =>
      (for {
        record <- req.as[PhoneRecord]
        _      <- PhoneBookService.update(id.toInt, record)
      } yield ())
        .foldM(err => BadRequest(err.getMessage()), result => Ok(result))
    case DELETE -> Root / id =>
      PhoneBookService
        .delete(id.toInt)
        .foldM(err => BadRequest("Not found"), result => Ok(result))
  }
}
