package net.gutefrage

import java.util.concurrent.atomic.AtomicLong

import com.twitter.finagle.Thrift
import com.twitter.finagle.thrift.Protocols
import com.twitter.util.Future

import net.gutefrage.temperature.thrift._

object TemperatureServer extends App {

  val port = args match {
    case Array(port) => port.toInt
    case Array() => 8080
    case _ => throw new IllegalArgumentException("usage: run [port]")
  }

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

  // run and announce the service
  // TODO try ThriftMux for server/consumer
  val server = Thrift.server
    .withLabel("temperature-service")
    .serveAndAnnounce(
      name = Services.temperatureServiceProvider,
      addr = s":$port",
      service = finagledService
    )

  // Keep waiting for the server and prevent the java process to exit

  System.in.read()
  server.close()

}
