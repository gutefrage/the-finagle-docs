package net.gutefrage


//servicePath: zk!127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183!/services/<service-name>/!0
//syntax:          schema!host!path!shardId
//schema:          for server, use zk, for client, use zk2
//host:            zookeeper connection string
//path:            service registration path in zookeeper
//shardId:         it's used internal
object Services {

  //change it to your zookeeper ip and port
  val zookeeperDest = "127.0.0.1:2181"


  private val exampleServicePath = "/services/example"
  val exampleServiceConsumer = buildConsumerPath(exampleServicePath)
  val exampleServiceProvider = buildProviderPath(exampleServicePath)

  private val temperatureServicePath = "/services/temperature"
  val temperatureServiceConsumer = buildConsumerPath(temperatureServicePath)
  val temperatureServiceProvider = buildProviderPath(temperatureServicePath)

  private val weatherServicePath = "/services/weather"
  val weatherServiceConsumer = buildConsumerPath(weatherServicePath)
  val weatherServiceProvider = buildProviderPath(weatherServicePath)

  def buildProviderPath(servicePath: String, zookeeperDest: String = zookeeperDest): String = {
    s"zk!$zookeeperDest!$servicePath!0"
  }

  // example states you should use `zk2`, but works with `zk` as well
  def buildConsumerPath(servicePath: String, zookeeperDest: String = zookeeperDest): String = {
    s"zk!$zookeeperDest!$servicePath"
  }

}
