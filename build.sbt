name := "Scala-IRC-bot"

scalaVersion := "2.13.0"

// Recommended flags from https://tpolecat.github.io/2017/04/25/scalac-flags.html (Removed scala 2.13 deprecated flags)
scalacOptions ++= Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
)
lazy val silencerVersion = "1.4.1"

lazy val ircBot = project.in(file("."))
  .settings(
    libraryDependencies += compilerPlugin(
      "com.github.ghik"        %% "silencer-plugin" % silencerVersion
    ),

    libraryDependencies ++= Seq(
      "org.pircbotx"            % "pircbotx"        % "2.1",
      "com.github.pureconfig"  %% "pureconfig"      % "0.11.1",
      "ch.qos.logback"          % "logback-classic" % "1.2.3",
      "com.typesafe.akka"      %% "akka-http"       % "10.1.9",
      "com.typesafe.akka"      %% "akka-actor"      % "2.5.23",
      "com.typesafe.akka"      %% "akka-stream"     % "2.5.23",
      "org.apache.commons"      % "commons-text"    % "1.7",
      "com.github.ghik"        %% "silencer-lib"    % silencerVersion,
      "org.scalatest"          %% "scalatest"       % "3.0.8" % "test",
    )
    
  )
  .settings(test in assembly := {})
  .settings(mainClass in assembly := Some("codes.co2.ircbot.pircbotx.Main"))
