package ru.arlen

import cats.effect.{ExitCode => CatsExitCode}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.arlen.api.PhoneBookApi
import ru.arlen.configuration._
import ru.arlen.services.PhoneBookService
import ru.arlen.services.PhoneBookService.PhoneBookService
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.{RIO, ZIO}

object Server {
  type AppEnvironment = PhoneBookService with Configuration with Clock with Blocking

  val appEnvironment = Configuration.live ++ PhoneBookService.doomy

  type AppTask[A] = RIO[AppEnvironment, A]

  val httpApp = Router[AppTask]("/phoneBook" -> new PhoneBookApi().route).orNotFound

  val server = for {
    config <- zio.config.getConfig[Config]
    // Get execution context
    server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      val ec = rts.platform.executor.asEC
      BlazeServerBuilder[AppTask](ec)
        .bindHttp(config.port, config.host)
        .withHttpApp(httpApp)
        .serve
        // компилируем стрим
        .compile[AppTask, AppTask, CatsExitCode]
        // отказываемся от результатов
        .drain
    }
  } yield server
}
