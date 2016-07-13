package net.gutefrage

import com.twitter.app.App
import com.twitter.conversions.time._
import com.twitter.finagle.ThriftMux
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Await, Future}
import net.gutefrage.temperature.thrift._
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Sends random temperature changes to the temperature service
  */
object TemperatureSensor extends App {

  import Env._
  val log = LoggerFactory.getLogger("application")

  val env = flag[Env]("env", Env.Local, "environment this server runs")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    log.info("Shutting down sensor")
  }

  def main(): Unit = {
    val client: TemperatureService.FutureIface = ThriftMux.client.newIface[TemperatureService.FutureIface](
      Services.buildConsumerPath("temperature", env()), "temperature-sensor")

    implicit val timer = DefaultTimer.twitter
    val randomTemp = new java.util.Random()

    def sendLoop: Future[Unit] = {
      val datum = TemperatureDatum(randomTemp.nextInt(40) - 10, System.currentTimeMillis / 1000)
      log.info(s"Sending data: $datum")
      for {
        _ <- Future.sleep(1.second)
        _ <- client.add(datum)
        _ <- sendLoop
      } yield ()
    }

    Await.ready(sendLoop)
  }

}
