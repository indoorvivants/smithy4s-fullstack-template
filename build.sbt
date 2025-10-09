import org.scalajs.linker.interface.Report
import org.scalajs.linker.interface.ModuleSplitStyle
import smithy4s.codegen.Smithy4sCodegenPlugin

Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions = new {
  val http4s = "0.23.30"

  val Scala = "3.7.3"

  val scribe = "3.17.0"

  val smithy4sFetch = "0.0.4"

  val TestContainers = "0.43.0"

  val Weaver = "0.10.1"

  val Playwright = "0.0.5"

  val Laminar = "17.2.1"

  val waypoint = "9.0.0"

  val circe = "0.14.5"

  val skunk = "1.0.0-M11"

  val macroTaskExecutor = "1.1.1"
}

val Config = new {
  val BasePackage = "hellosmithy4s"
}

lazy val root = project
  .in(file("."))
  .aggregate(backend.projectRefs*)
  .aggregate(shared.projectRefs*)
  .aggregate(frontend.projectRefs*)

lazy val backend = projectMatrix
  .in(file("modules/backend"))
  .dependsOn(shared)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % Versions.http4s,
      "com.disneystreaming.smithy4s" %%% "smithy4s-http4s" % smithy4sVersion.value,
      "com.outr"     %%% "scribe"       % Versions.scribe,
      "com.outr"     %%% "scribe-cats"  % Versions.scribe,
      "com.outr"     %%% "scribe-slf4j" % Versions.scribe,
      "org.tpolecat" %%% "skunk-core"   % Versions.skunk,
      "dev.rolang"   %%% "dumbo"        % "0.6.0"
    ),
    Compile / doc / sources := Seq.empty,
    reStart / baseDirectory := (ThisBuild / baseDirectory).value,
    run / baseDirectory     := (ThisBuild / baseDirectory).value,
    (Compile / compile) := ((Compile / compile) dependsOn (Compile / copyResources)).value
  )

lazy val shared = projectMatrix
  .in(file("modules/shared"))
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .jsPlatform(Seq(Versions.Scala))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %%% "smithy4s-http4s" % smithy4sVersion.value
    )
  )

lazy val frontend = projectMatrix
  .in(file("modules/frontend"))
  .jsPlatform(Seq(Versions.Scala))
  .defaultAxes((defaults :+ VirtualAxis.js)*)
  .dependsOn(shared)
  .enablePlugins(ForgeViteWebappPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.raquo"    %%% "waypoint"       % Versions.waypoint,
      "com.raquo"    %%% "laminar"        % Versions.Laminar,
      "io.circe"     %%% "circe-core"     % Versions.circe,
      "io.circe"     %%% "circe-parser"   % Versions.circe,
      "tech.neander" %%% "smithy4s-fetch" % Versions.smithy4sFetch,
      "org.scala-js" %%% "scala-js-macrotask-executor" % Versions.macroTaskExecutor
    )
  )

lazy val tests = projectMatrix
  .in(file("modules/tests"))
  .dependsOn(backend)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .settings(
    libraryDependencies ++= Seq(
      // test dependencies
      "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.TestContainers % Test,
      "org.typelevel" %%% "weaver-cats"         % Versions.Weaver % Test,
      "org.http4s"    %%% "http4s-ember-server" % Versions.http4s % Test,
      "org.http4s"    %%% "http4s-ember-client" % Versions.http4s % Test,
      "dev.rolang"    %%% "dumbo"               % "0.6.0",
      "com.indoorvivants.playwright" %% "weaver" % Versions.Playwright % Test
    ),
    Test / fork := true,
    Test / envVars += "FRONTEND_DIST" -> ((frontend.js(
      Versions.Scala
    ) / sourceDirectory).value.getParentFile() / "dist").toString,
    Compile / doc / sources := Seq.empty
  )

lazy val defaults =
  Seq(VirtualAxis.scalaABIVersion(Versions.Scala), VirtualAxis.jvm)

val playwrightTests = taskKey[Unit]("")
lazy val cmd        = s"${Config.BasePackage}.tests.playwright.*"
Global / playwrightTests := Def
  .sequential(
    Seq(
      (frontend.js(Versions.Scala) / frontendBuild).toTask(""),
      (tests.jvm(Versions.Scala) / Test / testOnly).toTask(" " + cmd)
    )
  )
  .value

addCommandAlias(
  "stubTests",
  s"tests/testOnly ${Config.BasePackage}.tests.stub.*"
)
addCommandAlias(
  "unitTests",
  s"tests/testOnly ${Config.BasePackage}.tests.unit.*"
)
addCommandAlias(
  "fastTests",
  s"tests/testOnly ${Config.BasePackage}.tests.stub.* ${Config.BasePackage}.tests.unit.*"
)
addCommandAlias(
  "integrationTests",
  s"tests/testOnly ${Config.BasePackage}.tests.integration.*"
)

ThisBuild / concurrentRestrictions ++= {
  if (sys.env.contains("CI")) {
    Seq(
      Tags.limitAll(4)
    )
  } else Seq.empty
}

ThisBuild / version ~= (_.replace('+', '-'))
ThisBuild / dynver ~= (_.replace('+', '-'))

lazy val versionDump =
  taskKey[Unit]("Dumps the version in a file named version")

versionDump := {
  val file = (ThisBuild / baseDirectory).value / "version"
  IO.write(file, (Compile / version).value)
}

import sbtwelcome.*

logo :=
  s"""
     | ##### ###### #    # #####  #        ##   ##### ######
     |   #   #      ##  ## #    # #       #  #    #   #
     |   #   #####  # ## # #    # #      #    #   #   #####
     |   #   #      #    # #####  #      ######   #   #
     |   #   #      #    # #      #      #    #   #   #
     |   #   ###### #    # #      ###### #    #   #   ######
     |
     |Version: ${version.value}
     |
     |${scala.Console.YELLOW}Scala ${(backend.jvm(
      true
    ) / scalaVersion).value}${scala.Console.RESET}
     |
     |""".stripMargin

logoColor := scala.Console.MAGENTA

usefulTasks := Seq(
  UsefulTask("ft", "fastTests", "Unit and stub tests - fast, only in memory"),
  UsefulTask(
    "it",
    "integrationTests",
    "Integration tests - run against Docker container, slower than fast"
  ),
  UsefulTask(
    "pt",
    "playwrightTests",
    "Playwright tests - verify frontend works in a browser, slower than slow"
  )
  // UsefulTask("pd", "publishDocker", "Publish app's docker container")
)
