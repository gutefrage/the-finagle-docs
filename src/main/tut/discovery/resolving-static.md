---
layout: docs
title:  "Static"
section: "servicediscovery"
---

## Static Resolving

The first option to create a client connection is by giving a static location.
The location string has the shape

```
scheme!arg
```

`Resolver.eval` will parse this directly to a `Name.Bound`. Thus no dynamic request routing is possible.
Finagle ships with a set of predefined schemes you can use.


### Inet (DNS)


The `inet` scheme uses DNS for resolving network addresses. It's also the default scheme, which means that
if you leave out a `scheme` in the location string, the [DnsResolver](https://github.com/twitter/finagle/blob/develop/finagle-core/src/main/scala/com/twitter/finagle/InetResolver.scala) is used.


#### Scheme

```
inet!www.scala-lang.org:80
```

Because `inet` is the default scheme you can also write

```
www.scala-lang.org:80
```

#### Example

```scala
import com.twitter.finagle.Http
val client = Http.newService("www.scala-lang.org:80")
```


### Fixed Inet (DNS)


InetResolver that caches all successful DNS lookups indefinitely and does not poll for updates.
Clients should only use this in scenarios where host -> IP map changes do not occur.

#### Scheme

```
fixedinet!www.scala-lang.org:80
```

### Zookeeper (zk)


> The Finagle documentation recommend using the zk2 scheme. See the section on zk2 below

The `zk` scheme resolves a `Serverset` from the given zookeeper instance and path.

The implementation can be found here: [ZkResolver](https://github.com/twitter/finagle/blob/develop/finagle-serversets/src/main/scala/com/twitter/finagle/zookeeper/ZkResolver.scala).

#### Scheme

```
zk!zookeeper-host!path
```

#### Example


```scala
import com.twitter.finagle.Http
val client = Http.newService("zk!127.0.0.1:2181!/path/to/service")
```

### Zookeeper (zk2)

The `zk2` scheme resolves a `Serverset` from the given zookeeper instance and path.

The implementation can be found here:
 [Zk2Resolver](https://github.com/twitter/finagle/blob/develop/finagle-serversets/src/main/scala/com/twitter/finagle/serverset2/Zk2Resolver.scala)

#### Scheme

```
zk2!zookeeper-host!path
```

#### Example

```scala
import com.twitter.finagle.Http
val client = Http.newService("zk2!127.0.0.1:2181!/path/to/service")
```
