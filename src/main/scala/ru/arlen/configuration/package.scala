package ru.arlen

import zio._
import zio.config.ReadError
import zio.config.typesafe.TypesafeConfig

package object configuration {
  case class Config(host: String, port: Int)

  import zio.config.magnolia.DeriveConfigDescriptor.descriptor

  val configDescriptor = descriptor[Config]

  type Configuration = zio.Has[Config]

  object Configuration {
    val live: Layer[ReadError[String], Configuration] =
      TypesafeConfig.fromDefaultLoader(configDescriptor)
  }
}
