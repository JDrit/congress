
name := "congress"

version := "0.1"

scalaVersion := "2.11.11"

mainClass in (Compile, run) := Some("net.batchik.congress.Main")

assemblyJarName in assembly := "congress.jar"

test in assembly := {}

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.2",
  "commons-io" % "commons-io" % "2.5")

fork in run := true

javaOptions ++= Seq("-Xmx1G", "-XX:+TieredCompilation", "-XX:+UseG1GC")
