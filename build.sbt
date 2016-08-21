name := "sbt-confluence"

organization := "de.lenabrueder"

//scalaVersion in Global := "2.11.8"

sbtPlugin := true

//confluenceBase := "http://localhost:8090/"
//confluenceUser := "admin"
//confluencePassword := "admin"

version := "0.1-SNAPSHOT"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.4.8", //this is the last one supporting sbt's scala version 2.10
  "org.asynchttpclient" % "async-http-client" % "2.0.12"
)
