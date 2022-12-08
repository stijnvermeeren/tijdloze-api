name := "De Tijdloze Website API"
maintainer := "Stijn Vermeeren"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(evolutions, guice, ws, ehcache, filters)
libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.48",
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "com.typesafe.play" %% "play-json-joda" % "2.9.3",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.2",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.pauldijou" %% "jwt-play-json" % "5.0.0",
  "ch.qos.logback" % "logback-classic" % "1.4.4"
)
