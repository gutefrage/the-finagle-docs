package net.gutefrage

import java.util.concurrent.atomic.AtomicLong

import com.twitter.finagle.{Thrift, ThriftMux}
import com.twitter.finagle.thrift.Protocols
import com.twitter.util.Future
import net.gutefrage.temperature.thrift._
import net.gutefrage.config._

object TemperatureServer extends App {

  val config = Config.parseServerConfig(args).getOrElse(sys.exit(1))

  // the actual server implementation
  val service = new TemperatureService.FutureIface {

    private val sum = new AtomicLong(0L)
    private val numDatums = new AtomicLong(0L)

    override def add(datum: TemperatureDatum): Future[Unit] = {
      sum.addAndGet(datum.celsius)
      numDatums.incrementAndGet()
      Future.Unit
    }

    override def mean(): Future[Double] = {
      val n = numDatums.get
      if(n == 0) Future.value(0.0) else Future.value(sum.get / n)
    }
  }

  val finagledService = new TemperatureService.FinagledService(
   service, Protocols.binaryFactory()
  )


  // Run the service implemented on the port 8080, but not announce it
  // val server = Thrift.serveIface(":8080", service)

  val serverProtocol = config.protocol match {
    case ThriftProtocol => Thrift.server
    case MuxProtocol => ThriftMux.server
  }

  // run and announce the service
  val server = serverProtocol
    .withLabel("temperature-service")
    .serveAndAnnounce(
      name = Services.temperatureServiceProvider,
      addr = s":${config.port}",
      service = finagledService
    )

  // Keep waiting for the server and prevent the java process to exit

  System.in.read()
  server.close()

}
