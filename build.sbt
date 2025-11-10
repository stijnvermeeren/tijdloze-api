name := "tijdloze.rocks API"
maintainer := "Stijn Vermeeren"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(evolutions, guice, ws, ehcache, filters)
libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.7.2",
  "org.playframework" %% "play-slick" % "6.1.1",
  "org.playframework" %% "play-slick-evolutions" % "6.1.0",
  "com.typesafe.play" %% "play-json-joda" % "2.10.6",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.9.1",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.pauldijou" %% "jwt-play-json" % "5.0.0",
  "ch.qos.logback" % "logback-classic" % "1.5.6",
  "io.burt" % "athena-jdbc" % "0.4.0",
  "org.apache.commons" % "commons-text" % "1.11.0"
)
