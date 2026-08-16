name := "tijdloze.rocks API"
maintainer := "Stijn Vermeeren"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.8.4"

libraryDependencies ++= Seq(evolutions, guice, ws, ehcache, filters)
libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.7.13",
  "org.playframework" %% "play-slick" % "6.2.0",
  "org.playframework" %% "play-slick-evolutions" % "6.2.0",
  "com.typesafe.play" %% "play-json-joda" % "2.10.8",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.9.1",
  "org.apache.commons" % "commons-email" % "1.6.0",
  "com.github.jwt-scala" %% "jwt-play-json" % "11.0.4",
  "ch.qos.logback" % "logback-classic" % "1.6.3",
  "org.apache.commons" % "commons-text" % "1.15.0"
)

PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-max-idle" -> "50 seconds"
