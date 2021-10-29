package ru.arlen.services

import zio.Has
import zio.Task
import ru.arlen.dto._
import zio.{ZLayer, ULayer}
import zio.ZIO
import zio.IO
import zio.macros.accessible

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

  // accessible или использовать макрос @accessible
//  def find(phone: String): ZIO[PhoneBookService, Option[Nothing], (Int, PhoneRecord)] =
//    ZIO.accessM(_.get.find(phone))
//  def insert(phoneRecord: PhoneRecord): Task[Int] =
//    ZIO.accessM(_.get.insert(phoneRecord))
//  def update(id: Int, phoneRecord: PhoneRecord): Task[Unit] =
//    ZIO.accessM(_.get.update(id, phoneRecord))
//  def delete(id: Int): IO[Option[Nothing], PhoneRecord] =
//    ZIO.accessM(_.get.delete(id))

}
