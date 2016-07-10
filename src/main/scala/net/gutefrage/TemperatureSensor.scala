package net.gutefrage

import com.twitter.app.App
import com.twitter.conversions.time._
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.{Thrift, ThriftMux}
import com.twitter.util.{Await, Future, FutureCancelledException}
import net.gutefrage.config._
import net.gutefrage.temperature.thrift._
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Sends random temperature changes to the temperature service
  */
object TemperatureSensor extends App {

  import TransportProtocol._

  val protocol = flag("protocol", ThriftMuxProtocol: Protocol, "Protocol to use: thrift | mux")

  val log = LoggerFactory.getLogger("application")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    log.info("Shutting down sensor")
  }

  def main(): Unit = {

    val client = protocol() match {
      case ThriftProtocol =>
        Thrift.newIface[TemperatureService.FutureIface](
            Services.temperatureServiceConsumer,
            "temperature-sensor")

      case ThriftMuxProtocol =>
        ThriftMux.newIface[TemperatureService.FutureIface](
            Services.temperatureServiceConsumer,
            "temperature-sensor-mux")
    }

    implicit val timer = DefaultTimer.twitter
    val randomTemp = new java.util.Random()

    def sendLoop: Future[Unit] = {
      val datum = TemperatureDatum(randomTemp.nextInt(40) - 10,
                                   System.currentTimeMillis / 1000)
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
