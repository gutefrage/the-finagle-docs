Http
====

Finagle provides APIs for writing http services and clients.


Server
------

A `minimal HTTP server`_ example is available in the Finagle docs. Essentially it's a `Finagle Service`_ that handles
``http.Request``s and returns ``http.Response``s.


.. code-block:: scala

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


Starting the http server with a default configuration.

.. code-block:: scala

  val server = Http.serve(":8080", service)
  Await.ready(server)


5xx responses as failures
~~~~~~~~~~~~~~~~~~~~~~~~~

Finagle is protocol agnostic so it treats a http ``5xx`` as a valid response. The behaviour can be configured with
a ``ResponseClassifier``.

.. code-block:: scala

  import com.twitter.finagle.http.service.HttpResponseClassifier

  val server = Http.server
    .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
    .serve(":8080", service)
  Await.ready(server)




Client
------

Creating a http client is done via ``Http.client``.


.. code-block:: scala

  import com.twitter.finagle.{Http, Service}
  import com.twitter.finagle.http

  val hostname = "gutefrage.net"
  val client: Service[http.Request, http.Response] =
    Http.client
        .newService(s"$hostname:80")


HTTPS
~~~~~

For a https connection change the port and add a TLS hostname.

.. code-block:: scala

  import com.twitter.finagle.{Http, Service}
  import com.twitter.finagle.http

  val hostname = "gutefrage.net"
  val client: Service[Request, Response] =
    Http.client
        .withTls(hostname)
        .newService(s"$hostname:443")


Service discovery
-----------------

In general DNS is used for service discovery when using http. However you can also use the zookeeper or other
:ref:`service-discovery` methods for http services.

.. _minimal HTTP server: https://twitter.github.io/finagle/guide/Quickstart.html#a-minimal-http-server
.. _Finch: https://github.com/finagle/finch
.. _Finagle Service: https://twitter.github.io/finagle/docs/#com.twitter.finagle.Service
