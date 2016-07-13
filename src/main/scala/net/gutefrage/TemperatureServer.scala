package net.gutefrage

import com.twitter.app.App
import com.twitter.logging.Logger
import org.slf4j.bridge.SLF4JBridgeHandler

object TemperatureServer extends App {

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    log.info("Shutting down temperature server")
  }

  val log = Logger("application")

  def main() {
    log.info(s"Starting temperature server")
  }

}
