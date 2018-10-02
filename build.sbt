
import Settings._

inThisBuild(List(
  organization := "sh.almond",
  homepage := Some(url("https://github.com/almond-sh/almond")),
  licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  developers := List(
    Developer(
      "alexarchambault",
      "Alexandre Archambault",
      "alexandre.archambault@gmail.com",
      url("https://github.com/alexarchambault")
    )
  )
))

lazy val logger = project
  .underShared
  .settings(
    shared,
    testSettings,
    libraryDependencies += Deps.scalaReflect.value
  )

lazy val channels = project
  .underShared
  .dependsOn(logger)
  .settings(
    shared,
    testSettings,
    libraryDependencies ++= Seq(
      Deps.fs2,
      Deps.jeromq
    )
  )

lazy val protocol = project
  .underShared
  .dependsOn(channels)
  .settings(
    shared,
    libraryDependencies += Deps.argonautShapeless
  )

lazy val `interpreter-api` = project
  .underShared
  .settings(
    shared
  )

lazy val interpreter = project
  .underShared
  .dependsOn(`interpreter-api`, protocol)
  .settings(
    shared,
    testSettings
  )

lazy val kernel = project
  .underShared
  .dependsOn(interpreter)
  .settings(
    shared,
    testSettings,
    libraryDependencies ++= Seq(
      Deps.caseAppAnnotations,
      Deps.fs2
    )
  )

lazy val `scala-kernel-api` = project
  .underScala
  .dependsOn(`interpreter-api`)
  .settings(
    shared,
    crossVersion := CrossVersion.full,
    generatePropertyFile("almond/almond.properties"),
    generateDependenciesFile,
    libraryDependencies += Deps.ammoniteRepl
  )

lazy val `scala-interpreter` = project
  .underScala
  .dependsOn(interpreter, `scala-kernel-api`, kernel % "test->test")
  .settings(
    shared,
    crossVersion := CrossVersion.full,
    testSettings
  )

lazy val `scala-kernel` = project
  .underScala
  .dependsOn(kernel, `scala-interpreter`)
  .settings(
    shared,
    crossVersion := CrossVersion.full,
    libraryDependencies += Deps.caseApp
  )

lazy val echo = project
  .underModules
  .dependsOn(kernel)
  .settings(
    shared,
    libraryDependencies += Deps.caseApp
  )

lazy val `almond-spark` = project
  .underScala
  .dependsOn(`scala-kernel-api` % "provided")
  .settings(
    shared,
    libraryDependencies ++= Seq(
      Deps.ammoniteRepl % "provided",
      Deps.ammoniteSpark,
      Deps.argonautShapeless,
      Deps.sparkSql % "provided"
    ),
    disableScalaVersion("2.12")
  )

lazy val almond = project
  .in(file("."))
  .aggregate(
    `almond-spark`,
    channels,
    echo,
    `interpreter-api`,
    interpreter,
    kernel,
    logger,
    protocol,
    `scala-interpreter`,
    `scala-kernel-api`,
    `scala-kernel`
  )
  .settings(
    shared,
    dontPublish
  )