name := "akka-learning"

version := "0.1.0"

scalaVersion := "2.13.12"

lazy val akkaVersion = "2.8.5"
lazy val akkaHttpVersion = "10.5.3"

libraryDependencies ++= Seq(
  // Akka Actor
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  
  // Akka Persistence
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
  
  // JDBC Persistence Plugin
  "com.lightbend.akka" %% "akka-persistence-jdbc" % "5.2.1",
  
  // PostgreSQL Driver
  "org.postgresql" % "postgresql" % "42.7.1",
  
  // Slick for database access
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  
  // Serialization
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.4.14",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  
  // Testing
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

// Compiler options
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint"
)
