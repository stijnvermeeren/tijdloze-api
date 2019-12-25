name := "De Tijdloze Website API"

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

assemblyJarName in assembly := "tijdloze-api.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    MergeStrategy.discard
  case classFile if classFile.contains(".class") =>
    // TODO Why do we need this to avoid assembly errors?
    MergeStrategy.first
  case manifest if manifest.contains("MANIFEST.MF") =>
    // We don't need manifest files since sbt-assembly will create
    // one with the given settings
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
    // Keep the content for all reference-overrides.conf files
    MergeStrategy.concat
  case x =>
    // For all the other files, use the default sbt-assembly merge strategy
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
