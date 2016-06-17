package net.gutefrage.config

import scopt._

sealed trait Protocol
case object ThriftProtocol extends Protocol
case object MuxProtocol extends Protocol

case class ClientConfig(protocol: Protocol)
case class ServerConfig(port: Int, protocol: Protocol)

// TODO may replace with com.twitter.app.App
// see https://github.com/twitter/finagle/blob/develop/finagle-example/src/main/scala/com/twitter/finagle/example/mysql/Example.scala#L42-L46
object Config {

  private implicit val protocolReads: scopt.Read[Protocol] = scopt.Read.reads {
    case "mux" => MuxProtocol
    case "thrift" => ThriftProtocol
    case pro => throw new IllegalArgumentException(s"Unkown protocol $pro")
  }

  def parseServerConfig(args: Seq[String]): Option[ServerConfig] =
    serverConfigParser.parse(args, ServerConfig(8080, ThriftProtocol))

  def parseClientConfig(args: Seq[String]): Option[ClientConfig] =
    clientConfigParser.parse(args, ClientConfig(ThriftProtocol))


  private  val serverConfigParser = new OptionParser[ServerConfig]("server-config") {
    head("Server Configuration", "1.0")
    opt[Int]('p', "port").action((port, conf) => conf.copy(port = port))
    opt[Protocol]('t', "protocol").action((proto, conf) => conf.copy(protocol = proto))
  }

  private  val clientConfigParser = new OptionParser[ClientConfig]("client-config") {
    head("Client Configuration", "1.0")
    opt[Protocol]('t', "protocol").action((proto, conf) => conf.copy(protocol = proto))
  }


}
