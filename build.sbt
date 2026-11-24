ThisBuild / organization := "com.dragishak"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.10.1"

val postgresVersion = "42.7.5"
val scodecVersion   = "2.3.3"
val configVersion   = "1.4.10"
val slf4jVersion    = "2.0.20"
val logbackVersion  = "1.6.8"
val fs2Version      = "3.16.0"

lazy val root = (project in file("."))
  .settings(
    name                 := "postgres-replication-client",
    Compile / run / fork := true,
    libraryDependencies ++= List(
      "org.postgresql" % "postgresql"      % postgresVersion,
      "org.scodec"    %% "scodec-core"     % scodecVersion,
      "com.typesafe"   % "config"          % configVersion,
      "org.slf4j"      % "slf4j-api"       % slf4jVersion,
      "org.slf4j"      % "jul-to-slf4j"    % slf4jVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "co.fs2"        %% "fs2-core"        % fs2Version
    )
  )
