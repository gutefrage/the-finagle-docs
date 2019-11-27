package net.gutefrage.context

import java.util.concurrent.atomic.AtomicLong

import com.twitter.finagle._
import com.twitter.finagle.thrift.Protocols
import com.twitter.logging.{Logger, Logging}
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}
import net.gutefrage.Env
import net.gutefrage.temperature.thrift._
import org.slf4j.bridge.SLF4JBridgeHandler


/**
  * In-memory implementation of the temperature service. It provides an example on how to
  * access finagle [[com.twitter.finagle.context.Contexts]]
  *
  * Extends [[com.twitter.server.TwitterServer]] to provide an admin interface.
  * See the README.md for start instructions.
  *
  */
object TemperatureServer extends TwitterServer with Logging{

  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  override def failfastOnFlagsNotParsed: Boolean = true

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
  }

  onExit {
    info("Shutting down temperature server")
  }

  val appLog = Logger("application")

  def main() {
    appLog.info(s"Starting temperature server in environment ${env().name} on port ${port()}")

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
        // Accessing a given userContext if provided
        UserContext.current.foreach { userContext =>
          appLog.info(s"Call received with context: ${userContext}")
        }

        val n = numElements.get()
        val mean = if(n == 0L) 0.0 else sumTemperature.get() / n
        Future.value(mean)
      }
    }

    // Wrap the service implementation into a real finagle service
    val finagledService: Service[Array[Byte], Array[Byte]] = new TemperatureService.FinagledService(
      service, Protocols.binaryFactory()
    )

    // run and announce the service
    val server = ThriftMux.server
      .withLabel("temperature-service")
      .serveAndAnnounce(
        // schema ! host ! path ! shardId
        name = s"zk!127.0.0.1:2181!/service/${env.name}/temperature!0",
        // bind to local address
        addr = s":${port()}",
        service = finagledService
      )

    // Keep waiting for the server and prevent the java process to exit
    closeOnExit(server)
    Await.ready(server)
  }

}
