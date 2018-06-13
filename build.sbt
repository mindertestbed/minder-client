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
  "log4j" % "log4j" % "1.2.16",
  "org.beybunproject" % "xoola" % "1.3.0",
  "com.yerlibilgin.minder" % "minder-common" % "1.1.0",
  "org.specs2" %% "specs2-junit" % "4.0.2" % Test
)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
