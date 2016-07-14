package net.gutefrage

import com.twitter.app.App
import com.twitter.finagle._
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import net.gutefrage.filter.DtabLogger
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

    // initialise our custom dtabs
    Dtabs.init()
  }

  onExit {
    log.info("Shutting down finch api server")
  }

  def main(): Unit = {
    log.info(s"Starting finch api server in ${env().name}")

    val client = ThriftMux.client.newIface[TemperatureService.FutureIface](
      Services.buildConsumerPath("temperature", env()),  "weather-api-client")

    /** json model */
    case class Mean(mean: Double)

    // api endpoint definition
    val api: Endpoint[Mean] = get("weather" / "mean") {
      client.mean().map(mean => Ok(Mean(mean)))
    }

    // start and announce the server
    val server = Http.server
      .withLabel("weather-api")
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .serveAndAnnounce(
        name = Services.buildProviderPath("weather", env()),
        addr = s":${port()}",
        service = new DtabLogger andThen api.toService
      )

    closeOnExit(server)
    Await.ready(server)

  }

}
