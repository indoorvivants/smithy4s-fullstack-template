addSbtPlugin(
  "com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % sys.env
    .getOrElse("SMITHY_VERSION", "0.17.11")
)
addSbtPlugin("io.spray"            % "sbt-revolver"           % "0.9.1")
addSbtPlugin("com.github.sbt"      % "sbt-native-packager"    % "1.9.16")
addSbtPlugin("com.eed3si9n"        % "sbt-projectmatrix"      % "0.9.1")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"            % "1.13.2")
addSbtPlugin("org.jmotor.sbt"      % "sbt-dependency-updates" % "1.2.8")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"           % "2.5.2")
addSbtPlugin("com.github.sbt"      % "sbt-dynver"             % "5.1.0")
addSbtPlugin("com.github.reibitto" % "sbt-welcome"            % "0.2.2")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
