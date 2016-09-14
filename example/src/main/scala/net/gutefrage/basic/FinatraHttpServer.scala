package net.gutefrage.basic

import javax.inject.{Inject, Singleton}

import com.google.inject.{Module, Provides}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.response.Mustache
import com.twitter.inject.TwitterModule
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
  */
class WeatherController @Inject()(
  temperatureService: TemperatureService.FutureIface
) extends Controller {

  get("/") { request: Request =>
    temperatureService.mean().map { meanTemperature =>
      DashboardData(meanTemperature, Some("Dashboard"))
    }
  }

  get("/none") { request: Request =>
    temperatureService.mean().map { meanTemperature =>
      response.ok.view(
        "dashboard.mustache",
        DashboardData(meanTemperature, None)
      )
    }
  }
}

@Mustache("dashboard")
case class DashboardData(meanTemperature: Double, name: Option[String])
