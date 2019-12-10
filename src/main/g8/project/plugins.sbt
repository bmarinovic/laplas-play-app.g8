addSbtPlugin("com.typesafe.sbt"     % "sbt-native-packager" % "1.3.20")
addSbtPlugin("org.scalameta"        % "sbt-scalafmt"        % "$scalaFmtVersion$")
addSbtPlugin("com.github.tototoshi" % "sbt-slick-codegen"   % "$slickCodegenPluginVersion$")
addSbtPlugin("com.typesafe.play"    % "sbt-plugin"          % "2.7.0")
// we need to add postgres connector for slick codegen plugin
libraryDependencies += "org.postgresql" % "postgresql" % "$postgresqlVersion$"