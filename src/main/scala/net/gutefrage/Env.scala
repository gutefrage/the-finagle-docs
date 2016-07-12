package net.gutefrage

import com.twitter.app.Flaggable




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
