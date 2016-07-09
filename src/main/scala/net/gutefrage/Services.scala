package net.gutefrage

import com.twitter.finagle.Dtab
import net.gutefrage.config.Env


//servicePath: zk!127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183!/services/<service-name>/!0
//syntax:          schema!host!path!shardId
//schema:          for server, use zk, for client, use zk2
//host:            zookeeper connection string
//path:            service registration path in zookeeper
//shardId:         it's used internal
object Services {

  //change it to your zookeeper ip and port
  val zookeeperDest = "127.0.0.1:2181"

  object Dtabs {
    val base = Dtab.read(
      """|/zk#  => /$/com.twitter.serverset;
         |/s##  => /zk#/127.0.0.1:2181;
         |/s#   => /s##/local;
         |/s    => /s#;""".stripMargin)
  }

  private val temperatureServicePath = "temperature"
  val temperatureServiceConsumer = buildConsumerPath(temperatureServicePath)
  def temperatureServiceProvider(env: Env) = buildProviderPath(env, temperatureServicePath)

  private val weatherServicePath = "weather"
  val weatherServiceConsumer = buildConsumerPath(weatherServicePath)
  def weatherServiceProvider(env: Env) = buildProviderPath(env, weatherServicePath)

  def buildProviderPath(env: Env, servicePath: String, zookeeperDest: String = zookeeperDest): String = {
    s"zk!$zookeeperDest!/${env.name}/$servicePath!0"
  }

  // example states you should use `zk2`, but works with `zk` as well
  def buildConsumerPath(servicePath: String, zookeeperDest: String = zookeeperDest): String = {
    s"zk!$zookeeperDest!/local/$servicePath"
  }

}
