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
          Libraries.squants,
          Libraries.doobieCore,
          Libraries.doobieHikari,
          Libraries.doobiePostgres,
          Libraries.doobieRefined,
          Libraries.doobieTest, 
          Libraries.doobieTestSpecs2,
          Libraries.typesafeConfig
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
          Libraries.squants,
          Libraries.typesafeConfig
        )
  )