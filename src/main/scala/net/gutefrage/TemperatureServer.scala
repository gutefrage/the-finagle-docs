package net.gutefrage


import com.twitter.app.App
import com.twitter.finagle.exp.Mysql
import com.twitter.finagle.exp.mysql._
import com.twitter.conversions.time._
import com.twitter.finagle._
import com.twitter.finagle.thrift.Protocols
import com.twitter.logging.Logger
import com.twitter.util.{Await, Future}
import net.gutefrage.temperature.thrift._
import net.gutefrage.config._

object TemperatureServer extends App {

  import TransportProtocol._

  val protocol = flag("protocol", ThriftMuxProtocol: Protocol, "Protocol to use: thrift | mux")
  val port = flag("port", 8080, "port this server should use")

  val log = Logger()

  def main() {

    // the actual server implementation
    val service = new TemperatureService.FutureIface {

      private val mysql = Mysql.client
        .withLabel("temperature-mysql-client")
        .withCredentials("weather", "weather")
        .withDatabase("weather")
        .newRichClient("localhost:3306")


      val create = mysql.query(
        """CREATE TABLE IF NOT EXISTS temperature_datums (
    id int(11) unsigned NOT NULL AUTO_INCREMENT,
    celsius int(11) DEFAULT NULL,
    timestamp DATETIME DEFAULT NOW(),
    PRIMARY KEY (id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8""")
        .onSuccess(r => log.info(s"Created database: $r"))
        .onFailure(f => log.error("Failed creating database", f))


      // await the creation of mysql tables
      Await.ready(create, 10.seconds)

      override def add(datum: TemperatureDatum): Future[Unit] = {
        mysql.query(s"""
      INSERT INTO temperature_datums (celsius, timestamp)
      VALUES (${datum.celsius}, from_unixtime(${datum.timestamp}))""").map { result =>
          log.info(s"Storing datum with result: $result")
          Unit
        }
      }

      override def mean(): Future[Double] = {
        mysql.query("SELECT avg(celsius) as avg FROM temperature_datums").map {
          case r: ResultSet =>
            val average = for {
              first <- r.rows.headOption
              avg <- first("avg")
            } yield avg match {
              case LongValue(v) => v.toDouble
              case DoubleValue(d) => d
              case RawValue(Type.NewDecimal, charset, isBinary, bytes) =>
                val str = new String(bytes, Charset(charset))
                str.toDouble // this makes me sad
              case v =>
                log.error(s"Received invalid mysql value: $v")
                0.0
            }
            average.getOrElse(0.0)
          case response =>
            log.error("Received not a result set")
            0.0
        }
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

    // run and announce the service
    val server = serverProtocol
      .withLabel("temperature-service")
      .serveAndAnnounce(
        name = Services.temperatureServiceProvider,
        addr = s":${port()}",
        service = new DtabLogger andThen finagledService
      )

    // Keep waiting for the server and prevent the java process to exit

    System.in.read()
    server.close()

  }

}
