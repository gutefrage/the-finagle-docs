package net.gutefrage.basic

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.response.Mustache

object FinatraHttpServer extends HttpServer {

  override def defaultFinatraHttpPort = ":9000"

  override protected def configureHttp(router: HttpRouter): Unit =
    router.filter[LoggingMDCFilter[Request, Response]]
          .filter[TraceIdMDCFilter[Request, Response]]
          .filter[CommonFilters]
          .add[WeatherController]
}

class WeatherController extends Controller {

  get("/") { request: Request =>
    response.ok.view(
      "dashboard.mustache",
      DashboardData(0.0, Some("Dashboard"))
    )
  }

  get("/none") { request: Request =>
    response.ok.view(
      "dashboard.mustache",
      DashboardData(0.0, None)
    )
  }
}

@Mustache("dashboard")
case class DashboardData(meanTemperature: Double, name: Option[String])
