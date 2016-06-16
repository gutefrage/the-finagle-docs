package net.gutefrage

import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.{Http, Thrift, ThriftMux}
import io.finch._
import net.gutefrage.config._
import net.gutefrage.temperature.thrift.TemperatureService

object FinchWeatherApi extends App {

  val config = Config.parseServerConfig(args).getOrElse(sys.exit(1))

  val client = config.protocol match {
    case ThriftProtocol =>
      Thrift.newIface[TemperatureService.FutureIface](
        Services.temperatureServiceConsumer, "temperature-sensor")

    case MuxProtocol => ThriftMux.newIface[TemperatureService.FutureIface](
      Services.temperatureServiceConsumer, "temperature-sensor-mux")
  }

  val api: Endpoint[String] = get("weather" / "mean") {
    client.mean().map(mean => Ok(s"mean is: $mean"))
  }


  val server = Http.server
      .withLabel("weather-api")
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
    .serveAndAnnounce(
      name = Services.weatherServiceProvider,
      addr = s":${config.port}",
      service = api.toService
    )

  // interrupt on keypress
  System.in.read()
  server.close()

}
