import Dependencies._ 

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "app.vizion"
ThisBuild / organizationName := "Vizion"

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .settings(
    name := "auth"
  )
  .aggregate(core, tests)

lazy val tests = (project in file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "auth-test-suite",
    scalacOptions += "-Ymacro-annotations",
    pomIncludeRepository := { _ => false},
    scalafmtOnCompile := true,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
          CompilerPlugins.betterMonadicFor,
          CompilerPlugins.contextApplied,
          CompilerPlugins.kindProjector,
          Libraries.scalaCheck,
          Libraries.scalaTest,
          Libraries.scalaTestPlus
        )
  )
  .dependsOn(core)

lazy val core = (project in file("modules/core"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "auth-core",
    packageName in Docker := "auth",
    scalacOptions += "-Ymacro-annotations",
    pomIncludeRepository := { _ => false},
    scalafmtOnCompile := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
          CompilerPlugins.betterMonadicFor,
          CompilerPlugins.contextApplied,
          CompilerPlugins.kindProjector,
          Libraries.cats,
          Libraries.catsEffect,
          Libraries.catsMeowMtlCore,
          Libraries.catsMeowMtlEffects,
          Libraries.catsRetry,
          Libraries.circeCore,
          Libraries.circeGeneric,
          Libraries.circeParser,
          Libraries.circeRefined,
          Libraries.cirisCore,
          Libraries.cirisEnum,
          Libraries.cirisRefined,
          Libraries.fs2,
          Libraries.http4sDsl,
          Libraries.http4sServer,
          Libraries.http4sClient,
          Libraries.http4sCirce,
          Libraries.http4sJwtAuth,
          Libraries.javaxCrypto,
          Libraries.log4cats,
          Libraries.logback % Runtime,
          Libraries.newtype,
          Libraries.redis4catsEffects,
          Libraries.redis4catsLog4cats,
          Libraries.refinedCore,
          Libraries.refinedCats,
          Libraries.skunkCore,
          Libraries.skunkCirce,
          Libraries.squants,
          "com.typesafe" % "config" % "1.4.0"
        )
  )

/* // SEPERATE TO MODULES LATER 

lazy val global = project
  .in(file("."))
  .aggregate(authApi, vizionAuth, tests)

val circeVersion = "0.12.3"
val circeConfig = "0.7.0"
val logback = "1.2.3"
val http4sVersion = "0.21.0"
val doobieVersion = "0.8.8"
val slf4j = "1.7.25"

lazy val authApi = project
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "authApi",
    pomIncludeRepository := { _ => false},
    libraryDependencies ++= Seq(
      "org.slf4j"    % "slf4j-api"         % slf4j,
      "org.tpolecat" %% "doobie-core"      % doobieVersion,
      "org.tpolecat" %% "doobie-h2"        % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
      "org.tpolecat" %% "doobie-quill"     % doobieVersion,
      "org.tpolecat" %% "doobie-specs2"    % doobieVersion % "test",
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"
    ),
    scalacOptions += "-Ypartial-unification"
  )

lazy val vizionAuth = project
  .dependsOn(authApi)
  .settings(
    name := "vizionAuth",
    pomIncludeRepository := { _ => false},
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s"            %% "http4s-circe"           % http4sVersion,
      "io.circe"              %% "circe-core"             % circeVersion,
      "io.circe"              %% "circe-generic"          % circeVersion,
      "io.circe"              %% "circe-config"           % circeConfig,
      "ch.qos.logback"        %  "logback-classic"        % logback,
      "com.github.pureconfig" %% "pureconfig" % "0.12.3"
    ),
    scalacOptions += "-Ypartial-unification"
  )
*/ 