package net.gutefrage.config

import com.twitter.app.Flaggable

sealed trait Protocol
object TransportProtocol {

  case object ThriftProtocol extends Protocol
  case object ThriftMuxProtocol extends Protocol

  implicit val flaggableProtocol = new Flaggable[Protocol] {
    override def parse(s: String): Protocol = s match {
      case "mux" | "thriftmux" => ThriftMuxProtocol
      case "thrift" => ThriftProtocol
    }
  }
}

