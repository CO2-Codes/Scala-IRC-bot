name := "Scala-IRC-bot"

ThisBuild / version := "1.2.0"

scalaVersion := "3.0.0"

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
)

lazy val ircBot = project.in(file("."))
  .settings(
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      // Use snapshot to prevent an issue with incompatible transitive dependencies.
      "com.github.pircbotx"    % "pircbotx"                    % "master-SNAPSHOT",
      "com.github.pureconfig" %% "pureconfig-core"             % "0.16.0",
      "ch.qos.logback"         % "logback-classic"             % "1.2.3",
      "com.typesafe.akka"     %% "akka-http"                   % "10.2.4" cross CrossVersion.for3Use2_13,
      "com.typesafe.akka"     %% "akka-actor"                  % "2.6.15" cross CrossVersion.for3Use2_13,
      "com.typesafe.akka"     %% "akka-stream"                 % "2.6.15" cross CrossVersion.for3Use2_13,
      "org.apache.commons"     % "commons-text"                % "1.9",
      "com.danielasfregola"   %% "twitter4s"                   % "7.0" cross CrossVersion.for3Use2_13,
      "com.google.api-client"  % "google-api-client"           % "1.32.1",
      "com.google.apis"        % "google-api-services-youtube" % "v3-rev20210624-1.32.1",
      "org.scalatest"         %% "scalatest"                   % "3.2.9" % "test",
    ),
  )
  .settings(assembly / test := {})
  .settings(assembly / mainClass := Some("codes.co2.ircbot.pircbotx.Main"))
  .settings(assembly / assemblyMergeStrategy := {
    case "module-info.class" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  })
