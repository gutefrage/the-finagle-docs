package net.gutefrage.basic

import com.twitter.finagle._
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import net.gutefrage.context.UserContext
import net.gutefrage.filter.DtabLogger
import net.gutefrage.temperature.thrift._
import net.gutefrage.{Dtabs, Env, Services}
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Serves the mean temperature at `/weather/mean`
  *
  * Client resolving is done via [[com.twitter.finagle.Dtab]]s. To see how static resolving via a schema
  * works, take a look at the [[TemperatureSensorStatic]] implementation.
  *
  * @see http framework: https://github.com/finagle/finch
  * @see json encoding: https://github.com/travisbrown/circe
  */
object WeatherApi extends TwitterServer {

  import Env._

  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  val appLog = LoggerFactory.getLogger("application")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // initialise our custom dtabs
    Dtabs.init()
  }

  onExit {
    appLog.info("Shutting down finch api server")
  }

  def main(): Unit = {
    appLog.info(s"Starting finch api server in ${env().name}")

    val client = ThriftMux.client.newIface[TemperatureService.FutureIface](
      dest = "/s/temperature",
      label = "weather-api-client"
    )

    /** json model */
    case class Mean(mean: Double)

    // mean temperature endpoint
    val api: Endpoint[Mean] = get("weather" / "mean") {
      client.mean().map(mean => Ok(Mean(mean)))
    }

    // start and announce the server
    val server = Http.server
      .withLabel("weather-api")
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .serve(
        addr = s":${port()}",
        service = api.toService
      )

    closeOnExit(server)
    Await.ready(server)

  }

}
