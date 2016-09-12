package net.gutefrage.basic

import com.twitter.finagle.ThriftMux
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{
  CommonFilters,
  LoggingMDCFilter,
  TraceIdMDCFilter
}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.response.Mustache
import net.gutefrage.Dtabs
import net.gutefrage.temperature.thrift.TemperatureService

object FinatraHttpServer extends HttpServer {

  premain {
    Dtabs.init()
  }

  override def defaultFinatraHttpPort = ":9000"

  override protected def configureHttp(router: HttpRouter): Unit =
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[WeatherController]
}

class WeatherController extends Controller {

  val client = ThriftMux.client.newIface[TemperatureService.FutureIface](
    dest = "/s/temperature",
    label = "finatra-weather"
  )

  get("/") { request: Request =>
    client.mean().map { meanTemperature =>
      DashboardData(meanTemperature, Some("Dashboard"))
    }
  }

  get("/none") { request: Request =>
    client.mean().map { meanTemperature =>
      response.ok.view(
        "dashboard.mustache",
        DashboardData(meanTemperature, None)
      )
    }
  }
}

@Mustache("dashboard")
case class DashboardData(meanTemperature: Double, name: Option[String])
