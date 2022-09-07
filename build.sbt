import org.scalajs.linker.interface.Report
import org.scalajs.linker.interface.ModuleSplitStyle
import smithy4s.codegen.Smithy4sCodegenPlugin

Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions = new {
  val http4s         = "0.23.15"
  val Scala          = "3.2.0"
  val scribe         = "3.10.3"
  val http4sDom      = "0.2.3"
  val Flyway         = "9.2.2"
  val Postgres       = "42.5.0"
  val TestContainers = "0.40.9"
  val Weaver         = "0.7.13"
  val Laminar        = "0.14.2"
  val waypoint       = "0.5.0"
  val scalacss       = "1.0.0"
  val circe          = "0.14.2"
  val doobie         = "1.0.0-RC2"
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
    scalaVersion := Versions.Scala,

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
      if (isRelease)
        Def.task[Seq[File]] {
          val (_, location) = (ThisBuild / frontendOutput).value

          val outDir = (Compile / resourceManaged).value / "assets"
          IO.listFiles(location).toList.map { file =>
            val (name, ext) = file.baseAndExt
            val out         = outDir / (name + "." + ext)

            IO.copyFile(file, out)

            out
          }
        }
      else Def.task { Seq.empty[File] }
    },
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Test / fork             := true,
    reStart / baseDirectory := (ThisBuild / baseDirectory).value,
    run / baseDirectory     := (ThisBuild / baseDirectory).value
  )

lazy val backend = projectMatrix
  .in(file("modules/backend"))
  .dependsOn(shared)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .settings(
    scalaVersion := Versions.Scala,
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "com.outr"     %% "scribe"          % Versions.scribe,
      "com.outr"     %% "scribe-cats"     % Versions.scribe,
      "com.outr"     %% "scribe-slf4j"    % Versions.scribe,
      "org.tpolecat" %% "doobie-core"     % Versions.doobie,
      "org.tpolecat" %% "doobie-postgres" % Versions.doobie
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
  .jsPlatform(Seq(Versions.Scala))
  .defaultAxes((defaults :+ VirtualAxis.js)*)
  .dependsOn(shared)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      import org.scalajs.linker.interface.OutputPatterns
      if (isRelease) config
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
      "org.http4s"                   %%% "http4s-dom"   % Versions.http4sDom
    )
  )

lazy val tests = projectMatrix
  .in(file("modules/tests"))
  .dependsOn(app)
  .defaultAxes(defaults*)
  .jvmPlatform(Seq(Versions.Scala))
  .settings(
    scalaVersion := Versions.Scala,
    libraryDependencies ++= Seq(
      // test dependencies
      "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.TestContainers % Test,
      "com.disneystreaming" %% "weaver-cats"         % Versions.Weaver   % Test,
      "org.http4s"          %% "http4s-ember-server" % Versions.http4s   % Test,
      "org.http4s"          %% "http4s-ember-client" % Versions.http4s   % Test,
      "org.postgresql"       % "postgresql"          % Versions.Postgres % Test,
      "org.flywaydb"         % "flyway-core"         % Versions.Flyway   % Test
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Test / fork             := true,
    Compile / doc / sources := Seq.empty
  )

lazy val defaults =
  Seq(VirtualAxis.scalaABIVersion(Versions.Scala), VirtualAxis.jvm)

lazy val frontendOutput = taskKey[(Report, File)]("")

lazy val frontendJS = frontend.js(Versions.Scala)

ThisBuild / frontendOutput := {
  if (isRelease)
    (frontendJS / Compile / fullLinkJS).value.data ->
      (frontendJS / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
  else
    (frontendJS / Compile / fastLinkJS).value.data ->
      (frontendJS / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
}

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

lazy val buildFrontend = taskKey[Unit]("")

buildFrontend := {
  val (_, folder) = frontendOutput.value
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
