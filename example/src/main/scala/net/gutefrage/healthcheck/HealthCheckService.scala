package net.gutefrage.healthcheck

import com.twitter.finagle.{Service, Status, ThriftMux}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.server.AdminHttpServer.Route
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}
import io.circe.{Encoder, Json}
import net.gutefrage.{Dtabs, Env}
import org.slf4j.bridge.SLF4JBridgeHandler
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

/**
  * Sends random temperature changes to the temperature service.
  *
  * Resolving is performed via a static schema lookup. Zookeeper will be used as a schema.
  */
object HealthCheckService extends TwitterServer {
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // initialise custom dtabs
    Dtabs.init()
  }

  onExit {
    log.info("Shutting down sensor")
  }

  /**
    * Provide a custom status json encoder to display the status as a simple string.
    */
  implicit val statusEncoder: Encoder[Status] = new Encoder[Status] {
    override def apply(a: Status): Json = Json.fromString(a.toString)
  }

  /**
    * Response class for a service
    * @param name - service name
    * @param healthy - true if healthy
    * @param status - service status
    */
  case class ServiceStatus(name: String, healthy: Boolean, status: Status)

  /**
   * A list of all services we want to check. This is all hard coded for the sake
   * of simplicity. In a real world scenario you should do better.
   */
  val services = List(
    "/s/temperature" -> "temperature-sensor-healthcheck-dtab",
    "zk2!127.0.0.1:2181!/service/local/temperature" -> "temperature-sensor-healthcheck-local",
    "zk2!127.0.0.1:2181!/service/prod/temperature" -> "temperature-sensor-healthcheck-prod"
  ).map {
    case (dest, label) => label -> ThriftMux.newService(dest, label)
  }

  val healthCheckHandler = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      // check all services
      val servicesStatus = services.map {
        case (label, service) => ServiceStatus(label, service.isAvailable, service.status)
      }

      // build a response with finch's Output type
      val response = Output
        .payload(servicesStatus)
        .toResponse(request.version)
      Future.value(response)
    }
  }

  /*
   * Adding new admin routes can be done by override the `routes` method.
   * The `handler` is simply a http Service.
   */
  override def routes: Seq[Route] = super.routes ++ Seq(
    Route(path = "/admin/custom/healthcheck", handler = healthCheckHandler, alias = "Healthcheck", group = Some("Custom"), includeInIndex = true)
  )

  def main(): Unit = {
    Await.ready(Future.never)
  }

}
