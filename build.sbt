organization := "gov.tubitak.minder"

name := "minder-client"

version := "0.0.3"

resolvers += "Eid public repository" at "http://eidrepo:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

crossPaths := false

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.scala-lang" % "scala-compiler" % "2.11.4",
  "log4j" % "log4j" % "1.2.16",
  "org.specs2" %% "specs2-junit" % "2.3.12",
  "org.beybunproject" % "xoola" % "1.0.0-RC3",
  "gov.tubitak.minder" % "minder-common" % "0.0.5",
  "com.xyz" % "sample-gw-wrapper" % "0.0.1" % "test"
)



publishTo := Some("eid releases" at "http://eidrepo:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
