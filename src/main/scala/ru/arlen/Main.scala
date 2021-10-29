package ru.arlen

import zio._

object Main extends App {
  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    Server.server
      .provideSomeLayer[ZEnv](Server.appEnvironment)
      .exitCode
}
