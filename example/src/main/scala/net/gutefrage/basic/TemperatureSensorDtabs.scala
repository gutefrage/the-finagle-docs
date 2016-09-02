package net.gutefrage.basic

import com.twitter.app.App
import com.twitter.conversions.time._
import com.twitter.finagle.ThriftMux
import com.twitter.finagle.util.DefaultTimer
import com.twitter.util.{Await, Future}
import net.gutefrage.temperature.thrift._
import net.gutefrage.{Dtabs, Env}
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Sends random temperature changes to the temperature service.
  *
  * Resolving is performed via [[com.twitter.finagle.Dtab]]s. In order to work
  * `Dtabs.init()` is called in `premain`, which set the base dtab for this process.
  */
object TemperatureSensorDtabs extends App {
  val log = LoggerFactory.getLogger("application")

  val env = flag[Env]("env", Env.Local, "environment this server runs")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // initialise custom dtabs
    Dtabs.init()
  }

  onExit {
    log.info("Shutting down sensor")
  }

  def main(): Unit = {
    // create a thrift client and resolve address via dtabs
    val client = ThriftMux.client.newIface[TemperatureService.FutureIface](
      dest = "/s/temperature",
      label = "temperature-sensor"
    )

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
