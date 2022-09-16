addSbtPlugin(
  "com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen" % sys.env
    .getOrElse("SMITHY_VERSION", "0.15.3")
)
addSbtPlugin("io.spray"         % "sbt-revolver"        % "0.9.1")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager" % "1.9.11")
addSbtPlugin("com.eed3si9n"     % "sbt-projectmatrix"   % "0.9.0")
addSbtPlugin("org.scala-js"     % "sbt-scalajs"         % "1.11.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"         % "0.6.3")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.4.6")
addSbtPlugin("com.dwijnand"     % "sbt-dynver"          % "4.1.1")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
