name := "De Tijdloze Website API"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(jdbc, guice, ws, ehcache, filters)
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.1"
libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.6.7"
libraryDependencies += "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.4"
libraryDependencies += "org.apache.commons" % "commons-email" % "1.2"

assemblyJarName in assembly := "tijdloze-api.jar"

assemblyMergeStrategy in assembly := {
  case "play/reference-overrides.conf" => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
