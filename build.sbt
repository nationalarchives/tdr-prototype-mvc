name := """transfer-digital-records"""
organization := "com.example"

version := "1.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.12.8", "2.11.12")

libraryDependencies += guice
libraryDependencies += "com.iheart" %% "ficus" % "1.4.3"
libraryDependencies += "com.mohiva" %% "play-silhouette" % "5.0.7"
libraryDependencies += "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.7"
libraryDependencies += "com.mohiva" %% "play-silhouette-persistence" % "5.0.7"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "software.amazon.awssdk" % "aws-sdk-java" % "2.7.11"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test
libraryDependencies +=  "com.github.jarlakxen" %% "drunk" % "2.5.0"
