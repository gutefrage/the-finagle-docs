package net.gutefrage

import java.util.concurrent.atomic.AtomicLong

import com.redis.RedisClient
import com.twitter.finagle._
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.thrift.Protocols
import com.twitter.logging.Logger
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}
import net.gutefrage.context.UserContext
import net.gutefrage.filter.{QueryCacheFilter, DtabLogger}
import net.gutefrage.temperature.thrift._
import org.slf4j.bridge.SLF4JBridgeHandler


object TemperatureServer extends TwitterServer {

  import Env._

  val port = flag[Int]("port", 8080, "port this server should use")
  val env = flag[Env]("env", Env.Local, "environment this server runs")

  override def failfastOnFlagsNotParsed: Boolean = true

  premain {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // initialise our custom dtabs
    Dtabs.init()
  }

  onExit {
    log.info("Shutting down temperature server")
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
        Contexts.broadcast.get(UserContext).foreach { userContext =>
          appLog.info(s"Getting mean for user ${userContext.userId}")
        }
        val n = numElements.get()
        val mean = if(n == 0L) 0.0 else sumTemperature.get() / n
        Future.value(mean)
      }
    }

    val finagledService = new TemperatureService.FinagledService(
      service, Protocols.binaryFactory()
    )

    val redisClient = new RedisClient("localhost", 6379)

    // run and announce the service
    val server = ThriftMux.server
      .withLabel("temperature-service")
      .serveAndAnnounce(
        name = Services.buildProviderPath("temperature", env()),
        addr = s":${port()}",
        service = new DtabLogger andThen new QueryCacheFilter(methodsToCache=Some(Seq("mean")), redisClient) andThen finagledService
      )

    // Keep waiting for the server and prevent the java process to exit
    closeOnExit(server)
    Await.ready(server)
  }

}
