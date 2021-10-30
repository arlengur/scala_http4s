scalacOptions += "-Ymacro-annotations"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala_http4s",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Dependencies.zio,
    libraryDependencies ++= Dependencies.http4sServer,
    libraryDependencies ++= Dependencies.circe,
    libraryDependencies ++= Dependencies.zioConfig,
    libraryDependencies ++= Seq(
      Dependencies.kindProjector
    ),
    addCompilerPlugin(Dependencies.kindProjector)
  )
