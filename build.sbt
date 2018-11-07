name := "De Tijdloze Website API"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(jdbc, guice, ws, ehcache, filters)
libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.34",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-json-joda" % "2.6.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0",
  "io.spray" %%  "spray-json" % "1.3.4",
  "org.apache.commons" % "commons-email" % "1.2",
  "com.pauldijou" %% "jwt-play-json" % "0.19.0"
)

assemblyJarName in assembly := "tijdloze-api.jar"

assemblyMergeStrategy in assembly := {
  case "play/reference-overrides.conf" => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
