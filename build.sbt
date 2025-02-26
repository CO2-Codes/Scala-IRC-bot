name := "Scala-IRC-bot"

ThisBuild / version := "1.3.3"

scalaVersion := "3.6.3"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explain-types", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Wconf:all=error",
  "-Xmax-inlines", "50"
)

val circeV = "0.14.10"
val sttpClientV = "3.10.3"

lazy val ircBot = project.in(file("."))
  .settings(
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      // Use snapshot because pircbotx releases are few and far between.
      "com.github.pircbotx"            % "pircbotx"                    % "master-SNAPSHOT",
      "com.github.pureconfig"         %% "pureconfig-core"             % "0.17.8",
      "ch.qos.logback"                 % "logback-classic"             % "1.5.16",
      "io.circe"                      %% "circe-core"                  % circeV,
      "io.circe"                      %% "circe-generic"               % circeV,
      "io.circe"                      %% "circe-parser"                % circeV,
      "com.softwaremill.sttp.client3" %% "core"                        % sttpClientV,
      "com.softwaremill.sttp.client3" %% "fs2"                         % sttpClientV,
      "com.softwaremill.sttp.client3" %% "circe"                       % sttpClientV,
      "org.apache.commons"             % "commons-text"                % "1.13.0",
      "com.google.api-client"          % "google-api-client"           % "2.7.2",
      "com.google.apis"                % "google-api-services-youtube" % "v3-rev20250128-2.0.0",
      "org.scalatest"                 %% "scalatest"                   % "3.2.19" % "test",
    ),
  )
  .settings(assembly / test := {})
  .settings(assembly / mainClass := Some("codes.co2.ircbot.pircbotx.Main"))
  .settings(assembly / assemblyMergeStrategy := {
    str: String =>
      if (str.endsWith("module-info.class")) {
        MergeStrategy.discard
      } else {
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(str)
      }
  })
