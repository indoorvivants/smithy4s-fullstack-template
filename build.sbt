import org.scalajs.linker.interface.Report
import org.scalajs.linker.interface.ModuleSplitStyle
import smithy4s.codegen.Smithy4sCodegenPlugin

Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions = new {
  val http4s = "0.23.19"

  val Scala = "3.2.2"

  val scribe = "3.11.5"

  val http4sDom = "0.2.7"

  val Flyway = "9.15.2"

  val Postgres = "42.7.4"

  val TestContainers = "0.40.9"

  val Weaver = "0.8.1"

  val Playwright = "0.0.5"

  val Laminar = "15.0.0-M7"

  val waypoint = "6.0.0-M5"

  val scalacss = "1.0.0"

  val circe = "0.14.5"

  val doobie = "1.0.0-RC2"

  val macroTaskExecutor = "1.1.1"

}

val Config = new {
  val DockerImageName = "smithy4s-fullstack"
  val DockerBaseImage = "eclipse-temurin:17"
  val BasePackage     = "hellosmithy4s"
}

lazy val root = project
  .in(file("."))
  .aggregate(backend.projectRefs*)
  .aggregate(shared.projectRefs*)
  .aggregate(frontend.projectRefs*)
  .aggregate(app.projectRefs*)

resolvers +=
  "Sonatype S01 OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

lazy val app = projectMatrix
  .in(file("modules/app"))
  .dependsOn(backend)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .enablePlugins(JavaAppPackaging)
  .settings(
    // Docker configuration
    dockerBaseImage         := Config.DockerBaseImage,
    Compile / doc / sources := Seq.empty,
    Docker / packageName    := Config.DockerImageName,

    // dependencies
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-ember-server" % Versions.http4s,
      "org.postgresql" % "postgresql"          % Versions.Postgres,
      "org.flywaydb"   % "flyway-core"         % Versions.Flyway
    ),
    // embedding frontend in backend's resources
    Compile / resourceGenerators += {
      Def.task[Seq[File]] {
        copyAll(
          frontendBundle.value,
          (Compile / resourceManaged).value / "assets"
        )
      }
    },
    reStart / baseDirectory := (ThisBuild / baseDirectory).value,
    run / baseDirectory     := (ThisBuild / baseDirectory).value
  )

def copyAll(location: File, outDir: File) = {
  IO.listFiles(location).toList.map { file =>
    val (name, ext) = file.baseAndExt
    val out         = outDir / (name + "." + ext)

    IO.copyFile(file, out)

    out
  }
}

lazy val backend = projectMatrix
  .in(file("modules/backend"))
  .dependsOn(shared)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .settings(
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "com.outr"     %% "scribe"          % Versions.scribe,
      "com.outr"     %% "scribe-cats"     % Versions.scribe,
      "com.outr"     %% "scribe-slf4j"    % Versions.scribe,
      "org.tpolecat" %% "doobie-core"     % Versions.doobie,
      "org.tpolecat" %% "doobie-postgres" % Versions.doobie,
      "org.tpolecat" %% "doobie-hikari"   % Versions.doobie
    ),
    Compile / doc / sources := Seq.empty
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
  .customRow(
    Seq(Versions.Scala),
    axisValues = Seq(VirtualAxis.js, BuildStyle.SingleFile),
    Seq.empty
  )
  .customRow(
    Seq(Versions.Scala),
    axisValues = Seq(VirtualAxis.js, BuildStyle.Modules),
    Seq.empty
  )
  .defaultAxes((defaults :+ VirtualAxis.js)*)
  .dependsOn(shared)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig := {
      val config = scalaJSLinkerConfig.value
      import org.scalajs.linker.interface.OutputPatterns
      if (virtualAxes.value.contains(BuildStyle.SingleFile)) config
      else
        config
          .withModuleSplitStyle(
            ModuleSplitStyle
              .SmallModulesFor(List(s"${Config.BasePackage}.frontend"))
          )
          .withModuleKind(ModuleKind.ESModule)
          .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
    },
    libraryDependencies ++= Seq(
      "com.raquo"                    %%% "waypoint"     % Versions.waypoint,
      "com.github.japgolly.scalacss" %%% "core"         % Versions.scalacss,
      "com.raquo"                    %%% "laminar"      % Versions.Laminar,
      "io.circe"                     %%% "circe-core"   % Versions.circe,
      "io.circe"                     %%% "circe-parser" % Versions.circe,
      "org.http4s"                   %%% "http4s-dom"   % Versions.http4sDom,
      "org.scala-js" %%% "scala-js-macrotask-executor" % Versions.macroTaskExecutor
    )
  )

lazy val tests = projectMatrix
  .in(file("modules/tests"))
  .dependsOn(app)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .settings(
    libraryDependencies ++= Seq(
      // test dependencies
      "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.TestContainers % Test,
      "com.disneystreaming" %% "weaver-cats"         % Versions.Weaver   % Test,
      "org.http4s"          %% "http4s-ember-server" % Versions.http4s   % Test,
      "org.http4s"          %% "http4s-ember-client" % Versions.http4s   % Test,
      "org.postgresql"       % "postgresql"          % Versions.Postgres % Test,
      "org.flywaydb"         % "flyway-core"         % Versions.Flyway   % Test,
      "com.indoorvivants.playwright" %% "weaver" % Versions.Playwright % Test
    ),
    Compile / resourceGenerators += {
      Def.task[Seq[File]] {
        copyAll(
          frontendBundle.value,
          (Compile / resourceManaged).value / "assets"
        )
      }
    },
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Test / fork             := true,
    Compile / doc / sources := Seq.empty
  )

lazy val defaults =
  Seq(VirtualAxis.scalaABIVersion(Versions.Scala), VirtualAxis.jvm)

lazy val frontendModules = taskKey[(Report, File)]("")
ThisBuild / frontendModules := (Def.taskIf {
  def proj = frontend.finder(BuildStyle.Modules)(
    Versions.Scala
  )

  if (isRelease)
    (proj / Compile / fullLinkJS).value.data ->
      (proj / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
  else
    (proj / Compile / fastLinkJS).value.data ->
      (proj / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
}).value

lazy val frontendBundle = taskKey[File]("")
ThisBuild / frontendBundle := (Def.taskIf {
  def proj = frontend.finder(BuildStyle.SingleFile)(
    Versions.Scala
  )

  if (isRelease) {
    val res = (proj / Compile / fullLinkJS).value
    (proj / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
  } else {
    val res = (proj / Compile / fastLinkJS).value
    (proj / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
  }
}).value

lazy val isRelease = sys.env.get("RELEASE").contains("yesh")

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

addCommandAlias(
  "playwrightTests",
  s"tests/testOnly ${Config.BasePackage}.tests.playwright.*"
)

addCommandAlias(
  "publishDocker",
  "app/Docker/publishLocal"
)

lazy val buildFrontend = taskKey[Unit]("")

buildFrontend := {
  val (_, folder) = frontendModules.value
  val buildDir    = (ThisBuild / baseDirectory).value / "build" / "frontend"

  val indexHtml = buildDir / "index.html"
  val out       = folder.getParentFile() / "index.html"

  import java.nio.file.Files

  if (!Files.exists(out.toPath) || IO.read(indexHtml) != IO.read(out)) {
    IO.copyFile(indexHtml, out)
  }
}

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
     |${scala.Console.YELLOW}Scala ${(app.jvm(
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
  ),
  UsefulTask("pd", "publishDocker", "Publish app's docker container")
)
