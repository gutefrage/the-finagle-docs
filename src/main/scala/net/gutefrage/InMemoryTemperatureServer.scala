package net.gutefrage

import java.util.concurrent.atomic.AtomicLong

import com.twitter.app.App
import com.twitter.conversions.time._
import com.twitter.finagle._
import com.twitter.finagle.exp.Mysql
import com.twitter.finagle.exp.mysql._
import com.twitter.finagle.thrift.Protocols
import com.twitter.logging.{Level, Logger}
import com.twitter.util.{Await, Future}
import net.gutefrage.config._
import net.gutefrage.temperature.thrift._
import org.slf4j.bridge.SLF4JBridgeHandler

object InMemoryTemperatureServer extends App {

  import TransportProtocol._
  import Env._

  val protocol = flag[Protocol]("protocol", ThriftMuxProtocol, "Protocol to use: thrift | mux")
  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    log.info("Shutting down temperature server")
  }

  val log = Logger("application")

  def main() {
    log.info(s"Starting temperature server in ${env().name}")

    // the actual server implementation
    val service = new TemperatureService.FutureIface {

      val numElements = new AtomicLong()
      val sumTemperature = new AtomicLong()

      override def add(datum: TemperatureDatum): Future[Unit] = {
        numElements.incrementAndGet()
        sumTemperature.addAndGet(datum.celsius)
        Future.Unit
      }

      override def mean(): Future[Double] = {
        val n = numElements.get()
        val mean = if(n == 0L) 0.0 else sumTemperature.get() / n
        Future.value(mean)
      }
    }

    val finagledService = new TemperatureService.FinagledService(
      service, Protocols.binaryFactory()
    )


    // Run the service implemented on the port 8080, but not announce it
    // val server = Thrift.serveIface(":8080", service)

    val serverProtocol = protocol() match {
      case TransportProtocol.ThriftProtocol => Thrift.server
      case TransportProtocol.ThriftMuxProtocol => ThriftMux.server
    }

    Dtab.base = Services.Dtabs.base


    // run and announce the service
    val server = serverProtocol
      .withLabel("temperature-service")
      .serveAndAnnounce(
        name = Services.temperatureServiceProvider(env()),
        addr = s":${port()}",
        service = new DtabLogger andThen finagledService
      )

    // Keep waiting for the server and prevent the java process to exit
    closeOnExit(server)
    Await.ready(server)
  }

}
