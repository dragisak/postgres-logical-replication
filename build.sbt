ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.4"

val postgresVersion = "42.7.5"
val scodecVersion   = "2.3.2"

lazy val root = (project in file("."))
  .settings(
    name := "postgres-replication",
    libraryDependencies ++= List(
      "org.postgresql" % "postgresql"  % postgresVersion,
      "org.scodec"    %% "scodec-core" % scodecVersion
    )
  )
