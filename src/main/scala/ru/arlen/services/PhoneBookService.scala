package ru.arlen.services

import ru.arlen.dto._
import zio.macros.accessible
import zio.{Has, IO, Task, ULayer, ZIO, ZLayer}

@accessible
object PhoneBookService {

  type PhoneBookService = Has[Service]

  trait Service {
    def find(phone: String): IO[Option[Nothing], (Int, PhoneRecord)]

    def insert(phoneRecord: PhoneRecord): Task[Int]

    def update(id: Int, phoneRecord: PhoneRecord): Task[Unit]

    def delete(id: Int): IO[Option[Nothing], PhoneRecord]
  }

  val doomy: ULayer[PhoneBookService] = ZLayer.succeed(
    new Service {
      var id = 0
      val map = scala.collection.mutable.Map[Int, PhoneRecord]()

      def find(phone: String): IO[Option[Nothing], (Int, PhoneRecord)] =
        ZIO.fromOption(map.find(_._2.phone == phone))

      def insert(phoneRecord: PhoneRecord): Task[Int] = ZIO.effect {
        id += 1
        map.put(id, phoneRecord)
        id
      }

      def update(id: Int, phoneRecord: PhoneRecord): Task[Unit] =
        ZIO.effect(map.update(id, phoneRecord))

      def delete(id: Int): IO[Option[Nothing], PhoneRecord] =
        ZIO.fromOption(map.remove(id))
    }
  )
}
