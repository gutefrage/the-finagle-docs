package net.gutefrage

import com.twitter.app.App
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.{Http, Service, Thrift, ThriftMux}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.io.Buf
import com.twitter.util.Future
import net.gutefrage.config._
import net.gutefrage.temperature.thrift.TemperatureService

object WeatherApi extends App {

  import TransportProtocol._

  val protocol = flag("protocol", ThriftMuxProtocol: Protocol, "Protocol to use: thrift | mux")
  val port = flag("port", 8080, "port this server should use")

  def main(): Unit = {

    val weatherService = new Service[Request, Response] {

      val client = protocol() match {
        case ThriftProtocol =>
          Thrift.newIface[TemperatureService.FutureIface](
            Services.temperatureServiceConsumer, "temperature-sensor")

        case ThriftMuxProtocol => ThriftMux.newIface[TemperatureService.FutureIface](
          Services.temperatureServiceConsumer, "temperature-sensor-mux")
      }


      def apply(request: Request): Future[Response] = {
        client.mean().map { mean =>
          val response = Response(Status.Ok)
          response.contentType = "text/plain"
          response.content = Buf.Utf8.apply(mean.toString)
          response
        }
      }
    }


    val server = Http.server
        .withLabel("weather-api")
        .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .serveAndAnnounce(
        name = Services.weatherServiceProvider,
        addr = s":${port()}",
        service = weatherService
      )

    // interrupt on keypress
    System.in.read()
    server.close()

  }

}
