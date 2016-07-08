package net.gutefrage

import com.twitter.app.App
import com.twitter.finagle._
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.naming.DefaultInterpreter
import com.twitter.logging.{Level, Logger}
import com.twitter.util.Await
import io.finch._
import net.gutefrage.config._
import net.gutefrage.temperature.thrift.TemperatureService

object FinchWeatherApi extends App {

  import TransportProtocol._
  import Env._

  val protocol = flag[Protocol]("protocol",
                                ThriftMuxProtocol,
                                "Protocol to use: thrift | mux")
  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  val log = Logger()
  log.setLevel(Level.INFO)

  def main(): Unit = {
    log.info(s"Starting finch api server in ${env().name}")

    // setting base dtab
    Dtab.base = Services.Dtabs.base

    val client = protocol() match {
      case ThriftProtocol =>
        Thrift.newIface[TemperatureService.FutureIface](
            "/s/temperature", /*Services.temperatureServiceConsumer*/ "temperature-server")

      case ThriftMuxProtocol =>
        ThriftMux.newIface[TemperatureService.FutureIface](
            "/s/temperature", /*Services.temperatureServiceConsumer*/ "temperature-server-mux")
    }

    val api: Endpoint[String] = get("weather" / "mean") {
      client.mean().map(mean => Ok(s"mean is: $mean"))
    }

    val server = Http.server
      .withLabel("weather-api")
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .serveAndAnnounce(
          name = Services.weatherServiceProvider(env()),
          addr = s":${port()}",
          service = api.toService
      )

    onExit {
      log.info("Shutting down finch api server")
    }
    closeOnExit(server)
    Await.ready(server)
  }

}
