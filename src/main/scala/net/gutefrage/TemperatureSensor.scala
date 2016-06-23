package net.gutefrage

import com.twitter.conversions.time._
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.{Thrift, ThriftMux}
import com.twitter.util.{Future, FutureCancelledException}
import net.gutefrage.config._
import net.gutefrage.temperature.thrift._

/**
  * Sends random temperature changes to the temperature service
  */
object TemperatureSensor extends App {

  val config = Config.parseClientConfig(args).getOrElse(sys.exit(1))

  val client = config.protocol match {
    case ThriftProtocol =>
      Thrift.newIface[TemperatureService.FutureIface](
        Services.temperatureServiceConsumer, "temperature-sensor")

    case MuxProtocol => ThriftMux.newIface[TemperatureService.FutureIface](
      Services.temperatureServiceConsumer, "temperature-sensor-mux")
  }

  implicit val timer = DefaultTimer.twitter
  val randomTemp = new java.util.Random()

  def sendLoop: Future[Unit] = {
    val datum = TemperatureDatum(randomTemp.nextInt(40) - 10, System.currentTimeMillis / 1000)
    println(s"Sending data: $datum")
    for {
      _ <- Future.sleep(1.second)
      _ <- client.add(datum)
      _ <- sendLoop
    } yield ()
  }

  // only run for a short amount of time
  val loop = sendLoop

  // interrupt on keypress
  System.in.read()
  loop.raise(new FutureCancelledException)


}
