package net.gutefrage.basic

import javax.inject.{Inject, Singleton}

import com.google.inject.{Module, Provides}
import com.twitter.finagle.Dtab
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.response.Mustache
import com.twitter.inject.TwitterModule
import com.twitter.inject.annotations.Flag
import net.gutefrage.Dtabs
import net.gutefrage.temperature.thrift.TemperatureService

object FinatraHttpServer extends HttpServer {

  premain {
    Dtabs.init()
  }

  override def modules: Seq[Module] = Seq(TemperatureServiceModule)

  override def defaultFinatraHttpPort = ":9000"

  override protected def configureHttp(router: HttpRouter): Unit =
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[WeatherController]
}

/**
  * Provide a thrift client as an injected service
  */
object TemperatureServiceModule extends TwitterModule {

  import com.twitter.finagle.ThriftMux

  @Provides
  @Singleton
  def provideTemperatureService(): TemperatureService.FutureIface = {
    ThriftMux.client.newIface[TemperatureService.FutureIface](
      dest = "/s/temperature",
      label = "finatra-weather"
    )
  }
}

/**
  * The actual controller
  *
  * @param temperatureService - Injected thrift client
  * @param localDocRoot - resource configuration
  * @param docRoot - resource configuration
  */
class WeatherController @Inject()(
  temperatureService: TemperatureService.FutureIface,
  @Flag("local.doc.root") localDocRoot: String,
  @Flag("doc.root") docRoot: String
) extends Controller {

  private val docConfig = DocConfig(localDocRoot, docRoot)

  get("/") { request: Request =>
    temperatureService.mean().map { meanTemperature =>
      DashboardData(meanTemperature, Some("local"), docConfig)
    }
  }

  // Change dtab according to the environment
  get("/:env") { request: Request =>
    val environment = request.params("env")

    Dtab.unwind {
      Dtab.local = Dtab.read(s"/env => /s#/$environment")
      temperatureService.mean().map { meanTemperature =>
        DashboardData(meanTemperature, Some(environment), docConfig)
      }
    }
  }

  // Asset route (for styles.css)
  get("/assets/:*") { request: Request =>
    response.ok.file("/assets/" + request.params("*"))
  }
}

@Mustache("dashboard")
case class DashboardData(
  meanTemperature: Double,
  environment: Option[String],
  docConfig: DocConfig
)
case class DocConfig(localDocRoot: String, docRoot: String)
