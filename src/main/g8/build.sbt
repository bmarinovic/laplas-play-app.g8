import play.sbt.routes.RoutesKeys
import com.github.tototoshi.sbt.slick.CodegenPlugin.autoImport.slickCodegenJdbcDriver
import sbt.Keys.libraryDependencies

RoutesKeys.routesImport += "core.api.QueryStringBinders._"
RoutesKeys.routesImport += "core.api.PaginationQueryStringBinder._"
RoutesKeys.routesImport += "core.api.Pagination"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      guice,
      compilerPlugin(library.silencerPlugin),
      library.silencer                 % Provided,
      library.chimney,
      library.postgresql,
      library.playSlick,
      library.playSlickEvolutions,
      library.slickJodaMapper,
      library.joda,
      library.jodaConvert,
      library.autoconfigMacros,
      library.laplasCommons,
      library.playJsonExtension,
      library.tsecCommon,
      library.tsecPassword,
      library.tsecMac,
      library.tsecJwtMac,
    ),
    slickCodegenDatabaseUrl := "jdbc:postgresql://localhost:5432/$name$",
    slickCodegenDriver := slick.jdbc.PostgresProfile,
    slickCodegenDatabaseUser := "postgres",
    slickCodegenDatabasePassword := "postgres",
    slickCodegenJdbcDriver := "org.postgresql.Driver",
    slickCodegenOutputPackage := "$organization$.$name$",
    slickCodegenOutputDir := file("./app"),
    slickCodegenExcludedTables := Seq("play_evolutions"),
    slickCodegenCodeGenerator := { model: slick.model.Model =>
      new slick.codegen.SourceCodeGenerator(model) {
        override def code =
          "import com.github.tototoshi.slick.PostgresJodaSupport._\n" +
            "import org.joda.time.DateTime\n" +
            super.code
        override def Table = new Table(_) {
          override def Column = new Column(_) {
            override def rawType = model.tpe match {
              case "java.sql.Timestamp" => "DateTime"
              case _                    => super.rawType
            }
          }
        }
      }
    },
    javaOptions in Universal ++= Seq(
      "-Dplay.server.pidfile.path=/dev/null"
    ),
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=target/.*;app/de/sparrow/spareparts/Tables.scala",
      s"-P:silencer:sourceRoots=\${baseDirectory.value.getCanonicalPath}"
    )
  ).enablePlugins(DockerPlugin, PlayScala, CodegenPlugin)

lazy val commonSettings = Seq(
  name := "$name$",
  organization := "$organization$",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.8",
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    //    "-Xfatal-warnings",                 // Fail the compilation if there are any warnings.
    "-Xfuture", // Turn on future language features.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match", // Pattern match may not be typesafe.
    "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification", // Enable partial unification in type constructor inference
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
    "-Ywarn-unused:params", // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates", // Warn if a private member is unused.
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  ),
  scalacOptions in (Compile, console) --= Seq(
    "-Ywarn-unused:imports",
    "-Xfatal-warnings"
  )
)

lazy val library =
  new {
    val joda = "joda-time" % "joda-time" % "$jodaVersion$"
    val playJson = "com.typesafe.play" %% "play-json" % "$playJsonVersion$"
    val playJoda = "com.typesafe.play" %% "play-json-joda" % "$playJsonVersion$"
    val silencerPlugin = "com.github.ghik" %% "silencer-plugin" % "$silencerVersion$"
    val silencer = "com.github.ghik" %% "silencer-lib" % "$silencerVersion$"
    val retry = "com.softwaremill.retry" %% "retry" % "$retryVersion$"
    val laplasCommons = "hr.laplacian.laplas" %% "laplas-commons" % "$laplasCommonsVersion$"

    // db layer
    val chimney = "io.scalaland" %% "chimney" % "$chimneyVersion$"
    val postgresql = "org.postgresql" % "postgresql" % "$postgresqlVersion$"
    val playSlick = "com.typesafe.play" %% "play-slick" % "$playSlickVersion$"
    val playSlickEvolutions = "com.typesafe.play" %% "play-slick-evolutions" % "$playSlickEvolutionsVersion$"
    val slickJodaMapper = "com.github.tototoshi" %% "slick-joda-mapper" % "$slickJodaMapperVersion$"
    val jodaConvert = "org.joda" % "joda-convert" % "$jodaConvertVersion$"
    val tsecCommon = "io.github.jmcardon" %% "tsec-common" % "$tsecVersion$"
    val tsecPassword = "io.github.jmcardon" %% "tsec-password" % "$tsecVersion$"
    val tsecMac = "io.github.jmcardon" %% "tsec-mac" % "$tsecVersion$"
    val tsecJwtMac = "io.github.jmcardon" %% "tsec-jwt-mac" % "$tsecVersion$"
    val playJsonExtension = "ai.x" %% "play-json-extensions" % "$playJsonExtensionVersion$"

    val autoconfigMacros = "io.methvin.play" %% "autoconfig-macros" % "$autoconfigMacrosVersion$"
  }

