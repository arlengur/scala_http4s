package ru.arlen

import cats.effect.{ExitCode => CatsExitCode}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.arlen.api.PhoneBookApi
import ru.arlen.configuration._
import ru.arlen.dao.repositiories.PhoneRecordRepository
import ru.arlen.db.{DBTransactor, Liqui, LiquibaseService}
import ru.arlen.services.PhoneBookService
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.random.Random
import zio.{RIO, ZIO}

object Server {
  // Clock и Blocking для запуска HTTP4
  // Liqui обертка над Liquibase
  // LiquibaseService сервис для миграции, напианныйц в терминах Liqui
  // Random для генерации uuid
  // DBTransactor позволяет осуществить взаимодействие с БД
  type AppEnvironment = PhoneBookService.PhoneBookService
    with PhoneRecordRepository.PhoneRecordRepository
    with Configuration
    with Clock
    with Blocking
    with Liqui
    with LiquibaseService
    with Random
    with DBTransactor

  val appEnvironment = Configuration.live >+> Blocking.live >+> DBTransactor.live >+> LiquibaseService.liquibaseLayer ++
    PhoneRecordRepository.live >+> PhoneBookService.live ++ LiquibaseService.live

  // тайп-алиас для HTTP4S так как там только 1 параметр
  type AppTask[A] = RIO[AppEnvironment, A]

  val httpApp = Router[AppTask]("/phoneBook" -> new PhoneBookApi().route).orNotFound

  val server = for {
    // получили конфиг
    config <- zio.config.getConfig[Config]
    // выполнили миграции
    _ <- LiquibaseService.performMigration
    server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      // rts зио рантайм, используем его чтобы получить execution context, чтобы запустить BlazeServer
      val ec = rts.platform.executor.asEC
      BlazeServerBuilder[AppTask](ec)
        .bindHttp(config.api.port, config.api.host)
        .withHttpApp(httpApp)
        .serve
        // компилируем стрим
        .compile[AppTask, AppTask, CatsExitCode]
        // отказываемся от результатов
        .drain
    }
  } yield server
}
