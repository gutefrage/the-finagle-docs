---
layout: docs
title:  "Zookeeper"
section: "servicediscovery"
---

## Zookeeper

Finagle has first class [Zookeeper](https://zookeeper.apache.org/) support. Both static and dynamic resolving with
dtabs make use of zookeeper to manage service locations. The core data structure for this purpose is the
[Serverset](http://twitter.github.io/commons/apidocs/com/twitter/common/zookeeper/ServerSet.html).

There two scheme implementations `zk` (used to announce a service) and `zk2` ( used to resolve a service ). Both
use the `Serverset` internally for persisting, updating or resolving service network locations. Resolving is describe
in more detail in the [Static Resolving](/discovery/static-resolving) section.


For the *Zookeeper* scheme the `name` parameter is extended to:

```
scheme!host!path!shardId
```

The shardId can always be set to zero. The Finagle documentation says it's only used for a few services
at twitter.

Example:

```
zk!127.0.0.1:2181!/path/to/service!0
```

## Announce a ThriftMux server

Announcing a ThriftMux server.

```scala
val service: Service[Array[Byte], Array[Byte]] = ???
val server = ThriftMux.server
  .serveAndAnnounce(
    name = s"zk!127.0.0.1:2181!/path/to/service!0",
    addr = ":8000",
    service = service
  )
```
