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

sealed trait Env {
  val name: String
}
object Env {

  case object Prod extends Env { val name = "prod"}
  case object Local extends Env { val name = "local"}

  implicit val flaggableEnv = new Flaggable[Env] {
    override def parse(env: String): Env = env match {
      case "prod" => Prod
      case "local" => Local
    }
  }
}
