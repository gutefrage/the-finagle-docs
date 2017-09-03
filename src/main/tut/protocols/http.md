---
layout: docs
title:  "Http"
section: "protocols"
---


## Http

Finagle provides APIs for writing http services and clients.


### Server

A [minimal HTTP server] example is available in the Finagle docs. Essentially it's a [Finagle Service] that handles
`http.Request`s and returns `http.Response`s.


```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}

// the http service
val service = new Service[http.Request, http.Response] {
  def apply(req: http.Request): Future[http.Response] =
    Future.value(
      http.Response(req.version, http.Status.Ok)
    )
}
```

Starting the http server with a default configuration.

```scala
val server = Http.serve(":8080", service)
Await.ready(server)
```

#### 5xx responses as failures

Finagle is protocol agnostic so it treats a http `5xx` as a valid response. The behaviour can be configured with
a `ResponseClassifier`.

```scala
import com.twitter.finagle.http.service.HttpResponseClassifier

val server = Http.server
  .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
  .serve(":8080", service)
Await.ready(server)
```



### Client

Creating a http client is done via `Http.client`.


```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http

val hostname = "gutefrage.net"
val client: Service[http.Request, http.Response] =
  Http.client
      .newService(s"$hostname:80")
```

#### HTTPS

For a https connection change the port and add a TLS hostname.

```scala
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http

val hostname = "gutefrage.net"
val client: Service[Request, Response] =
  Http.client
      .withTls(hostname)
      .newService(s"$hostname:443")
```

#### Web Proxy


>    There is built-in support for tunneling TCP-based protocols through web proxy servers in a default Finagle client
>    that might be used with any TCP traffic, not only HTTP(S). See Squid documentation on this feature.
>
>    The following example enables tunneling HTTP traffic through a web proxy server my-proxy-server.com
>    to twitter.com.
>
>    -- [HTTP Proxy documentation example]


```scala
import com.twitter.finagle.{Service, Http}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.client.Transporter
import java.net.SocketAddress

val twitter: Service[Request, Response] = Http.client
  .configured(Transporter.HttpProxy(Some(
     new InetSocketAddress("my-proxy-server.com", 3128)
  )))
  .withSessionQualifier.noFailFast
  .newService("gutefrage.net")

```

### Service discovery

In general DNS is used for service discovery when using http. However you can also use the zookeeper or other
[service-discovery](/service-discovery.html) methods for http services.

[minimal HTTP server]: https://twitter.github.io/finagle/guide/Quickstart.html#a-minimal-http-server
[Finch]: https://github.com/finagle/finch
[Finagle Service]: https://twitter.github.io/finagle/docs/#com.twitter.finagle.Service
[HTTP Proxy documentation example]: https://twitter.github.io/finagle/guide/Clients.html#http-proxy
