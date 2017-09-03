---
layout: docs
title:  "DTabs"
section: "servicediscovery"
---

## Delegation Tables (DTabs)

Finagle provides a way to resolve services per request. The main concept are so called  **Dtabs** short for
**Delegation tables**. They provide a way to resolve a hierarchical path into a physical network address. The
Finagle documentation provides a detailed explanation on how [Paths] are evaluated and how dtabs are structured.

This section gives some examples on how to create, use and override dtabs.


## Dtabs in a nutshell

Oliver Gould sums it up in his talk [The Once and Future Layer 5] like this.

1. Applications refer to *logical names*
   Example: `/s/users`
2. Requests are bound to *concrete names*
   Example: `/#/zookeeper.example.local/prod/users`
3. *Delegations* express routing
   Example: `/s => /#/zookeeper.example.local/prod`

### Applications refer to logical names

In contrast to static resolving this means a *destination* is describe as unix-like, hierarchical path without any
information about schema, hosts or ports.

| Static Name.Bound                     | Logical Name.Path |
|---------------------------------------|-------------------|
| zk!zookeeper.example.local/prod/users | /s/users |

### Requests are bound to concrete names

A `Name.Path` is turned into a concrete `Path.Bound` via a delegation table. If the `Name.Path` cannot be
resolved the request can't be performed.

The Resolver can turn a `Name.Path` only into a `Path.Bound` if the resolving terminates with a [Namer]
implementation. A path starting with `$` are called "system paths" and are treated specially by Finagle.
This is used to specify a concrete [Namer], e.g.

- resolving inet addresses `/$/inet/localhost/8080/api/users`
- resolving [serversets][Serverset] in zookeeper `/$/com.twitter.serverset/127.0.0.1:2181/prod/users`


### Delegations express routing

Delegation tables are responsible for the dynamic part. They express the routing for a request by defining the path
rewrite rules. Dtabs rewrite a given path by a simple prefix substitution.

Given a path `/s/users` and  a delegation table with a single entry: `/s => /s#/env` resolving will generate
`/s#/env/users`.

Resolving as three important properties.

1. Dtab resolving must always terminate
    This means your delegation table must **not** have any **recursive** rules
2. Dtabs are applied from bottom to top
    This means you override previous rules by appending to an existing dtab
3. Dtabs are applied per request
    This means if you override an entry it will be used for the residual request tree


Usage
-----

First, you need to create an actual Dtab. Finagle provides various ways to instantiate a Dtab. The simplest variant
is to parse a string that has the correct format.

```scala
import com.twitter.finagle.Dtab

val delegationTable = Dtab.read(
    """|/zk##    => /$/com.twitter.serverset;
       |/zk#     => /zk##/127.0.0.1:2181;
       |/s#      => /zk#/service;
       |/env     => /s#/local;
       |/s       => /env;""".stripMargin
```

Finagle has two places to store Dtabs, `base` and `local`. `base` should be set once during startup and not
change afterwards. `local` is for one-time, local routing. Both places are accessed via the `Dtab` instance.


```scala
// set the base Dtab
Dtab.base = delegationTable

// set a local Dtab permanently
Dtab.local = delegationTable

// set a scoped, local Dtab and restore old Dtab afterwards
Dtab.unwind {
   Dtab.local = delegationTable
   // requests in this scope use the local Dtab
}
```

[Namer]: http://twitter.github.io/finagle/docs/#com.twitter.finagle.Namer
[Serverset]: http://twitter.github.io/commons/apidocs/com/twitter/common/zookeeper/ServerSet.html
[Zookeeper]: https://zookeeper.apache.org/
[Paths]: http://twitter.github.io/finagle/guide/Names.html#paths
[The Once and Future Layer 5]: https://speakerdeck.com/olix0r/the-once-and-future-layer-5-twitter-style-microservices-at-scale-with-finagle-and-linkerd-number?slide=31
