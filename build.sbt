name := "De Tijdloze Website API"
maintainer := "Stijn Vermeeren"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(jdbc, guice, ws, ehcache, filters)
libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.48",
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-json-joda" % "2.8.1",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.0",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.pauldijou" %% "jwt-play-json" % "4.2.0"
)
