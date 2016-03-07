organization := "gov.tubitak.minder"

name := "minder-client"

version := "0.4.4"

resolvers += "Eid public repository" at "http://193.140.74.199:8081/nexus/content/groups/public/"

resolvers += Resolver.mavenLocal

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in (doc) ++= Seq("-source", "1.8")

crossPaths := false

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.7",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang" % "scala-actors" % "2.11.7",
  "org.scala-lang" % "scala-compiler" % "2.11.7",
  "log4j" % "log4j" % "1.2.16",
  "org.beybunproject" % "xoola" % "1.2.0",
  "gov.tubitak.minder" % "minder-common" % "0.4.3",
  "org.specs2" %% "specs2-junit" % "2.3.12" % "test"
)



publishTo := Some("eid releases" at "http://193.140.74.199:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
