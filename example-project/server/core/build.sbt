import Dependencies._ 

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / organization     := "app.vizion"
ThisBuild / organizationName := "Vizion"

resolvers += Resolver.sonatypeRepo("snapshots")


val logback = "1.2.3"
val slf4j = "1.7.25"
val cats          = "2.0.0"
val http4sVersion = "0.21.0"
val circeConfig = "0.7.0"

lazy val global = project
  .in(file("."))
  .settings(
    name := "example-project" 
  )
  .aggregate(exampleProjectCore, exampleProjectAPI)

lazy val exampleProjectAPI = project
  .in(file("exampleProjectAPI"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "exampleProjectAPI",
    packageName in Docker := "exampleProjectApi",
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
      Libraries.doobieCore,
      Libraries.doobieHikari,
      Libraries.doobiePostgres,
      Libraries.doobieRefined,
      Libraries.doobieTest,
      Libraries.doobieTestSpecs2,
      Libraries.newtype,
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsRetry,
      Libraries.catsMeowMtl,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.log4cats,
      Libraries.typesafeConfig,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    )
  )

lazy val exampleProjectCore = project
  .dependsOn(exampleProjectAPI)
  .settings(
    name := "exampleProjectCore",
    scalacOptions += "-Ymacro-annotations",
    pomIncludeRepository := { _ => false},
    scalafmtOnCompile := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      CompilerPlugins.betterMonadicFor,
      CompilerPlugins.contextApplied,
      CompilerPlugins.kindProjector,
      Libraries.http4sDsl,
      Libraries.http4sClient,
      Libraries.http4sServer,
      Libraries.http4sCirce,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeRefined,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.newtype,
      Libraries.cats,
      Libraries.catsMeowMtlCore,
      Libraries.catsEffect,
      Libraries.catsRetry,
      Libraries.typesafeConfig,
      InternalLibraries.authentication   
    ),
  )