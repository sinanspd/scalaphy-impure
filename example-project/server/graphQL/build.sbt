ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "app.vizion"

val calibanVersion = "0.8.1"
val circeConfig = "0.7.0"
val logback = "1.2.3"
val slf4j = "1.7.25"
val fs2= "2.2.1"

lazy val global = project
  .in(file("."))
  .settings(
    name := "graphQLServer",
    pomIncludeRepository := { _ => false},
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-http4s" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-cats" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-monix" % calibanVersion,
      "io.circe"              %% "circe-config"  % circeConfig,
      "ch.qos.logback"        %  "logback-classic"        % logback,
      "co.fs2" %% "fs2-core" % fs2,
      "co.fs2" %% "fs2-io" % fs2,
      "co.fs2" %% "fs2-io" % fs2,
      "co.fs2" %% "fs2-reactive-streams" % fs2,
      "co.fs2" %% "fs2-experimental" % fs2,
      "dev.zio" %% "zio-interop-cats" % "2.1.3.0-RC15",
      "com.github.pureconfig" %% "pureconfig" % "0.12.3",
      "io.circe" %% "circe-core" % "0.12.3",
      "io.circe" %% "circe-generic" % "0.12.3",
      "io.circe" %% "circe-parser" % "0.12.3",
      "exampleprojectcore" %% "exampleprojectcore" % "0.0.1"
    )
  )