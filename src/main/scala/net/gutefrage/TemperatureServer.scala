package net.gutefrage

import com.twitter.app.App
import com.twitter.logging.Logger
import org.slf4j.bridge.SLF4JBridgeHandler

object TemperatureServer extends App {

  import Env._

  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    log.info("Shutting down temperature server")
  }

  val log = Logger("application")

  def main() {
    log.info(s"Starting temperature server in environment ${env().name} on port ${port()}")
  }

}
