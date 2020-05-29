import Dependencies._ 

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "app.vizion"
ThisBuild / organizationName := "Vizion"

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .settings(
    name := "authentication"
  )
  .aggregate(auth, authenticationApi)

val doobieVersion = "0.8.8"

lazy val authenticationApi = (project in file("authenticationApi"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "authenticationApi",
    packageName in Docker := "authenticationApi",
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
          "com.typesafe" % "config" % "1.4.0",
          "org.tpolecat" %% "doobie-core"      % doobieVersion,
          "org.tpolecat" %% "doobie-h2"        % doobieVersion,
          "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
          "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
          "org.tpolecat" %% "doobie-quill"     % doobieVersion,
          "org.tpolecat" %% "doobie-specs2"    % doobieVersion % "test",
          "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",
          "org.tpolecat" %% "doobie-refined" % doobieVersion,
)
  )

lazy val auth = (project in file("auth"))
  .dependsOn(authenticationApi)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "auth",
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