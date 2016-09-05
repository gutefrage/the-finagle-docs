package net.gutefrage.basic

import com.twitter.conversions.time._
import com.twitter.finagle.ThriftMux
import com.twitter.finagle.util.DefaultTimer
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}
import net.gutefrage.temperature.thrift._
import net.gutefrage.{Dtabs, Env}
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Sends random temperature changes to the temperature service.
  *
  * Resolving is performed via a static schema lookup. Zookeeper will be used as a schema.
  */
object TemperatureSensorStatic extends TwitterServer {
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
      // schema ! args
      dest = s"zk2!127.0.0.1:2181!/service/${env().name}/temperature",
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
