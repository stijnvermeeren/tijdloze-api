name := "tijdloze.rocks API"
maintainer := "Stijn Vermeeren"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(evolutions, guice, ws, ehcache, filters)
libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "8.0.33",
  "org.playframework" %% "play-slick" % "6.0.0",
  "org.playframework" %% "play-slick-evolutions" % "6.0.0",
  "com.typesafe.play" %% "play-json-joda" % "2.10.3",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.8.0",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.pauldijou" %% "jwt-play-json" % "5.0.0",
  "ch.qos.logback" % "logback-classic" % "1.4.4",
  "io.burt" % "athena-jdbc" % "0.4.0",
  "org.apache.commons" % "commons-text" % "1.11.0"
)
