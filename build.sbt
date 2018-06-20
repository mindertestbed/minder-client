organization := "com.yerlibilgin.minder"

name := "minder-client"

version := "1.1.0"

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += Resolver.mavenLocal

javacOptions in(Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in (doc) ++= Seq("-source", "1.8")

crossPaths := false

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "gov.tubitak" % "xoola" % "1.3.4",
  "com.yerlibilgin.minder" % "minder-common" % "1.1.0",
  "org.specs2" %% "specs2-junit" % "4.0.2" % Test
)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
