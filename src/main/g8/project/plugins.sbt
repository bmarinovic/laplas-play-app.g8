addSbtPlugin("com.typesafe.sbt"     % "sbt-native-packager" % "1.3.20")
addSbtPlugin("org.scalameta"        % "sbt-scalafmt"        % "2.0.0")
addSbtPlugin("com.typesafe.play"    % "sbt-plugin"          % "2.7.0")
addSbtPlugin("com.github.tototoshi" % "sbt-slick-codegen"   % "1.4.0")
addSbtPlugin("org.scoverage"        % "sbt-scoverage"       % "1.5.1")
// we need to add postgres connector for slick codegen plugin
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"
