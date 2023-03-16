addSbtPlugin(
  "com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % sys.env
    .getOrElse("SMITHY_VERSION", "0.17.5")
)
addSbtPlugin("io.spray"            % "sbt-revolver"           % "0.9.1")
addSbtPlugin("com.github.sbt"      % "sbt-native-packager"    % "1.9.16")
addSbtPlugin("com.eed3si9n"        % "sbt-projectmatrix"      % "0.9.0")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"            % "1.13.0")
addSbtPlugin("org.jmotor.sbt"      % "sbt-dependency-updates" % "1.2.7")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"           % "2.5.0")
addSbtPlugin("com.dwijnand"        % "sbt-dynver"             % "4.1.1")
addSbtPlugin("com.github.reibitto" % "sbt-welcome"            % "0.2.2")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
