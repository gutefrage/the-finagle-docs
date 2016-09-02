package net.gutefrage.context

import com.twitter.finagle._
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import net.gutefrage.temperature.thrift._
import net.gutefrage.{Dtabs, Env}
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Serves the mean temperature at `/weather/mean` and a endpoint with a given user under `/weather/mean/user/:userId`
  *
  * Client resolving is done via [[com.twitter.finagle.Dtab]]s. To see how static resolving via a schema
  * works, take a look at the [[net.gutefrage.basic.TemperatureSensorStatic]] implementation.
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
    case class MeanForUser(mean: Double, userId: Long)

    // mean temperature endpoint
    val mean: Endpoint[Mean] = get("weather" / "mean") {
      client.mean().map(mean => Ok(Mean(mean)))
    }

    // user endpoint which sets a UserContext
    val userMean: Endpoint[MeanForUser] =
      get("weather" / "mean" / "user" :: long) { userId: Long =>
        val userContext = UserContext(userId)
        Contexts.broadcast.let(UserContext, userContext) {
          client.mean().map(mean => Ok(MeanForUser(mean, userId)))
        }
      }

    // compose endpoints
    // https://github.com/finagle/finch/blob/master/docs/endpoint.md#composing-endpoints
    val api = mean :+: userMean

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
