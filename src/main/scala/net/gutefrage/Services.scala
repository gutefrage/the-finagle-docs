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

  /**
    * Build a concrete path for a service announcement.
    *
    * @param serviceName The name under which the service should be announced
    * @param env The environment in which the service should be announced
    * @param zookeeperDest Zookeeper destination, e.g. 127.0.0.1:2181
    * @return a concrete path the service can be announced on
    */
  def buildProviderPath(serviceName: String, env: Env, zookeeperDest: String = zookeeperDest): String = {
    s"zk!$zookeeperDest!/service/${env.name}/$serviceName!0"
  }

  /**
    * Build a concrete path to consume a service
    *
    * @param serviceName The name under which the service is announced
    * @param env The environment in which the service should be consumed
    * @param zookeeperDest Zookeeper instance to look for the service, e.g. 127.0.0.1:2181
    * @return a concrete path to lookup up a service
    */
  def buildConsumerPath(serviceName: String, env: Env, zookeeperDest: String = zookeeperDest): String = {
    // example states you should use `zk2`, but works with `zk` as well
    s"zk!$zookeeperDest!/service/{$env.name}/$serviceName"
  }

}
