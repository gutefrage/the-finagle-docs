package net.gutefrage.basic

import com.google.inject.Module
import com.twitter.finagle.ThriftMux
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.response.Mustache
import net.gutefrage.Dtabs
import net.gutefrage.finatra.{HandelbarsScala, HandlebarModules, HandlebarsJava}
import net.gutefrage.temperature.thrift.TemperatureService

import scala.beans.BeanProperty

object FinatraHttpServer extends HttpServer {

  premain {
    Dtabs.init()
  }

  override def defaultFinatraHttpPort = ":9000"

  override def modules: Seq[Module] = Seq(
    HandlebarModules
  )

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

  get("/dashboard/hbs/java") { request: Request =>
    DashboardDataJava(1.0)
  }

  get("/dashboard/hbs/scala") { request: Request =>
    DashboardDataScala(2.0)
  }
}

@Mustache("dashboard")
case class DashboardData(meanTemperature: Double, name: Option[String])

// we need the bean property so the java implementation can look up the getters
@HandlebarsJava("dashboard")
case class DashboardDataJava(@BeanProperty meanTemperature: Double)

@HandelbarsScala("dashboard")
case class DashboardDataScala(meanTemperature: Double)