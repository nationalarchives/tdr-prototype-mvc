import rocks.muki.graphql.schema.SchemaLoader
import sbt.File
import sbt.Keys.scalaVersion

name := """transfer-digital-records"""
organization := "com.example"

version := "1.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala,GraphQLCodegenPlugin)

graphqlSchemas += GraphQLSchema(
  "tdr",
  "tdr schema",
  Def.task(
    SchemaLoader.fromFile(new File("conf/schema.graphql")).loadSchema()
  ).taskValue
)
graphqlCodegenStyle := Apollo
graphqlCodegenJson := JsonCodec.Circe
graphqlCodegenSchema := graphqlRenderSchema.toTask("tdr").value
graphqlCodegenImports ++= List("java.time._")

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.12.8", "2.11.12")

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"
resolvers += Resolver.bintrayRepo("jarlakxen", "maven")
resolvers += Resolver.jcenterRepo


libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" %  "0.9.3",
  "io.circe" %% "circe-generic" %  "0.9.3",
  "io.circe" %% "circe-parser" %  "0.9.3",
  "org.sangria-graphql" %% "sangria" % "1.4.2",
)
libraryDependencies += guice
libraryDependencies += "com.iheart" %% "ficus" % "1.4.3"
libraryDependencies += "com.mohiva" %% "play-silhouette" % "5.0.7"
libraryDependencies += "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.7"
libraryDependencies += "com.mohiva" %% "play-silhouette-persistence" % "5.0.7"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "software.amazon.awssdk" % "aws-sdk-java" % "2.7.11"
libraryDependencies += "ca.ryangreen" % "apigateway-generic-java-sdk" % "1.3"
libraryDependencies +=  "com.github.jarlakxen" %% "drunk" % "2.5.0"
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.7" % "test"
)
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % "test"
)
javaOptions in Test += "-Dconfig.file=conf/application.test.conf"