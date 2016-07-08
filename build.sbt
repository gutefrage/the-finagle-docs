name := "finagle-weather"
version := "1.0"
organization := "net.gutefrage"

scalaVersion  := "2.11.7"

val finagleVersion = "6.35.0"
val finchVersion = "0.10.0"

libraryDependencies ++= Seq(
  "com.twitter" %% "scrooge-core" % "4.7.0",
  "com.twitter" %% "finagle-serversets" % finagleVersion,
  "com.twitter" %% "finagle-redis" % finagleVersion,
  "com.twitter" %% "finagle-mysql" % finagleVersion,
  "com.twitter" %% "finagle-thrift"  % finagleVersion,
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "finagle-thriftmux"  % finagleVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion
)

fork in run := true
