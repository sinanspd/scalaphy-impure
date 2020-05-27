name := "example-project"

ThisBuild / scalaVersion := "2.13.1"

val http4sVersion = "0.21.0"
val doobieVersion = "0.8.8"
val circeVersion = "0.12.3"
val circeConfig = "0.7.0"
val logback = "1.2.3"
val slf4j = "1.7.25"
val cats          = "2.0.0"
val catsEffect    = "2.0.0"
val catsMeowMtl   = "0.4.0"
val catsRetry     = "1.0.0"
val newtype       = "0.4.3"
val betterMonadicFor = "0.3.1"
val contextApplied   = "0.1.2"
val kindProjector    = "0.11.0"
val refined       = "0.9.10"
val log4cats      = "1.0.1"

lazy val global = project
  .in(file("."))
  .aggregate(exampleProjectCore, exampleProjectCoreAPI)

lazy val exampleProjectCoreAPI = project
  .settings(
    name := "exampleProjectCoreAPI",
    pomIncludeRepository := { _ => false},
    libraryDependencies ++= Seq(
      "org.slf4j"    % "slf4j-api"         % slf4j,
      "org.tpolecat" %% "doobie-core"      % doobieVersion,
      "org.tpolecat" %% "doobie-h2"        % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
      "org.tpolecat" %% "doobie-quill"     % doobieVersion,
      "org.tpolecat" %% "doobie-specs2"    % doobieVersion % "test",
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",
      "org.tpolecat" %% "doobie-refined" % doobieVersion, 
      "org.typelevel"    %% "cats-core"     % cats,
      "com.olegpy"       %% "meow-mtl-core" % catsMeowMtl,
      "org.typelevel"    %% "cats-effect"   % catsEffect,
      "com.github.cb372" %% "cats-retry"    % catsRetry,
      "io.estatico"       %% "newtype"        % newtype, 
      "eu.timepit" %% "refined"      % refined,
      "eu.timepit" %% "refined-cats" % refined,
      "io.chrisdavenport" %% "log4cats-slf4j" % log4cats,
      compilerPlugin("com.olegpy"     %% "better-monadic-for" % betterMonadicFor),
      compilerPlugin("org.augustjune" %% "context-applied"    % contextApplied),
      compilerPlugin("org.typelevel"  %% "kind-projector"     % kindProjector cross CrossVersion.full)      
    ),
    scalacOptions += "-Ymacro-annotations"
  )

lazy val minervaCore = project
  .dependsOn(minervaCoreApi)
  .settings(
    name := "minervaCore",
    pomIncludeRepository := { _ => false},
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s"            %% "http4s-circe"           % http4sVersion,
      "io.circe"              %% "circe-core"             % circeVersion,
      "io.circe"              %% "circe-generic"          % circeVersion,
      "io.circe"              %% "circe-config"           % circeConfig,
      "io.circe"              %% "circe-refined"           % circeVersion,
      "ch.qos.logback"        %  "logback-classic"        % logback,
      "org.typelevel"    %% "cats-core"     % cats,
      "com.olegpy"       %% "meow-mtl-core" % catsMeowMtl,
      "org.typelevel"    %% "cats-effect"   % catsEffect,
      "com.github.cb372" %% "cats-retry"    % catsRetry,
      "io.estatico"       %% "newtype"        % newtype, 
      "eu.timepit" %% "refined"      % refined,
      "eu.timepit" %% "refined-cats" % refined,
      "app.vizion" %% "auth-core" % "0.0.1",
      compilerPlugin("com.olegpy"     %% "better-monadic-for" % betterMonadicFor),
      compilerPlugin("org.augustjune" %% "context-applied"    % contextApplied),
      compilerPlugin("org.typelevel"  %% "kind-projector"     % kindProjector cross CrossVersion.full)      
   
    ),
    scalacOptions += "-Ymacro-annotations"
  )