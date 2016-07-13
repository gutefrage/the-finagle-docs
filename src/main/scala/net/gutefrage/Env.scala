package net.gutefrage

import com.twitter.app.Flaggable

/**
  * Environment a service runs in.
  */
sealed trait Env {
  val name: String
}
object Env {

  case object Prod extends Env { val name = "prod"}
  case object Local extends Env { val name = "local"}

  implicit val flaggableEnv = new Flaggable[Env] {
    override def parse(env: String): Env = env match {
      case Prod.name => Prod
      case Local.name => Local
    }
  }
}
