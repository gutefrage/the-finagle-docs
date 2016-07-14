package net.gutefrage

import com.twitter.app.App
import com.twitter.finagle._
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import net.gutefrage.temperature.thrift._
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Serves the mean temperature at `/weather/mean`
  */
object WeatherApi extends App {

  import Env._

  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  val log = LoggerFactory.getLogger("application")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    log.info("Shutting down finch api server")
  }

  def main(): Unit = {
    log.info(s"Starting finch api server in ${env().name}")

  }

}
