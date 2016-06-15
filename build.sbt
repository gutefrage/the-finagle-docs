name := "finagle-weather"
version := "1.0"
organization := "net.gutefrage"

scalaVersion  := "2.11.7"

libraryDependencies ++= Seq(
  "com.twitter" %% "scrooge-core" % "4.7.0",
  "com.twitter" %% "finagle-serversets" % "6.35.0",
  "com.twitter" %% "finagle-redis" % "6.35.0",
  "com.twitter" %% "finagle-mysql" % "6.35.0",
  "com.twitter" %% "finagle-thrift"  % "6.35.0",
  "com.twitter" %% "finagle-http" % "6.35.0",
  "com.twitter" %% "finagle-thriftmux"  % "6.35.0"
)

