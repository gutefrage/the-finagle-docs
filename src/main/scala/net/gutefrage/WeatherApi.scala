package net.gutefrage

import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.{Http, Service, Thrift}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.io.Buf
import com.twitter.util.Future
import net.gutefrage.temperature.thrift.TemperatureService

object WeatherApi extends App {

  val port = args match {
    case Array(port) => port.toInt
    case Array() => 8000
    case _ => throw new IllegalArgumentException("usage: run [port]")
  }


  val weatherService = new Service[Request, Response] {

    val client = Thrift.newIface[TemperatureService.FutureIface](
      Services.temperatureServiceConsumer, "weather-api")

    def apply(request: Request): Future[Response] = {
      client.mean().map { mean =>
        val response = Response.apply(Status.Ok)
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
      addr = s":$port",
      service = weatherService
    )

  // interrupt on keypress
  System.in.read()
  server.close()

}
