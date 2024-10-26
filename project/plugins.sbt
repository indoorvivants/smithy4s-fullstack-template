addSbtPlugin(
  "com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % sys.env
    .getOrElse("SMITHY_VERSION", "0.17.11")
)
addSbtPlugin("io.spray"            % "sbt-revolver"           % "0.9.1")
addSbtPlugin("com.github.sbt"      % "sbt-native-packager"    % "1.10.4")
addSbtPlugin("com.eed3si9n"        % "sbt-projectmatrix"      % "0.9.1")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"            % "1.17.0")
addSbtPlugin("org.jmotor.sbt"      % "sbt-dependency-updates" % "1.2.9")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"           % "2.5.2")
addSbtPlugin("com.github.sbt"      % "sbt-dynver"             % "5.1.0")
addSbtPlugin("com.github.reibitto" % "sbt-welcome"            % "0.2.2")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
