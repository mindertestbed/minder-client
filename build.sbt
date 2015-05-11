organization := "gov.tubitak.minder"

name := "minder-client"

version := "0.0.5"

resolvers += "Eid public repository" at "http://eidrepo:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in (doc) ++= Seq("-source", "1.8")

crossPaths := false

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.scala-lang" % "scala-compiler" % "2.11.4",
  "log4j" % "log4j" % "1.2.16",
  "org.specs2" %% "specs2-junit" % "2.3.12",
  "org.beybunproject" % "xoola" % "1.0.0",
  "gov.tubitak.minder" % "minder-common" % "0.0.5"
)



publishTo := Some("eid releases" at "http://eidrepo:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
