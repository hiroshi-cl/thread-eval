import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "hiroshi-cl",
      scalaVersion := "2.12.0",
      scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-explaintypes", "-Xlint", "-opt:l:classpath")
    )),
    name := "thread-eval",
    libraryDependencies += scalaTest % Test
  )
