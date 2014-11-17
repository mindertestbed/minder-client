organization := "gov.tubitak.minder"

name := "minder-client"

version := "0.0.1"

resolvers += "Eid public repository" at "http://eidrepo:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.specs2" %% "specs2-junit" % "2.3.12",
  "org.beybunproject" % "xoola" % "1.0.0-RC1",
  "gov.tubitak.minder" %% "minder-common" % "0.0.1", 
  "com.xyz" % "sampleminderwrapper" % "0.0.1" % "test"
)
