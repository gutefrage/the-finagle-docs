.. _service-discovery:

#################
Service Discovery
#################

.. caution:: This section is under construction.

Finagle provides different features to resolve client addresses. All implementations take care of the inherent
complexity of a distributed system.

This section provides a short explanation and some examples for demonstration.


Resolver & Names
================

Finagle uses *Names* to identify network locations. The `Names and Naming`_ section on the finagle documentation
provides a comprehensible introduction.

.. _Names and Naming: http://twitter.github.io/finagle/guide/Names.html


Announcing
==========


Protocols that support implementing a server (like Thrift or Http) usually provide a ``serveAndAnnounce`` method,
which gives the ability to announce the service under a given *Name*.

The service *Name* string looks similar to the one used for resolving ( see `Static Resolving` ):

.. code-block:: bash

    scheme!name

Zookeeper
---------

Finagle has first class `Zookeeper`_ support. Both static and dynamic resolving with dtabs make use of zookeeper
to manage service locations. The core data structure for this purpose is the `Serverset`_.

There two scheme implementations ``zk`` (used to announce a service) and ``zk2`` ( used to resolve a service ). Both
use the `Serverset`_ internally for persisting, updating or resolving service network locations. Resolving is describe
in more detail in the `Static Resolving`_ section.


For the *Zookeeper* scheme the ``name`` parameter is extended to:

.. code-block:: bash

    scheme!host!path!shardId

The shardId can always be set to zero. The Finagle documentation says it's only used for a few services
at twitter.

Example:

.. code-block:: bash

    zk!127.0.0.1:2181!/path/to/service!0


MDNS
----

For the *MDNS* scheme the ``name`` parameter is extended to:

.. code-block:: bash

    scheme![name]._[group]._tcp.local.

Example:

.. code-block:: bash

    mdns!myservice._twitter._tcp.local.

The implementation can be found here: `MDNS`_

Examples
--------

Announcing a ThriftMux server.

.. code-block:: scala

    val service: Service[Array[Byte], Array[Byte]] = ???
    val server = ThriftMux.server
      .serveAndAnnounce(
        name = s"zk!127.0.0.1:2181!/path/to/service!0",
        addr = ":8000",
        service = service
      )


Static Resolving
================

The first option to create a client connection is by giving a static location.
The location string has the shape

.. code-block:: bash

    scheme!arg

``Resolver.eval`` will parse this directly to a ``Name.Bound``. Thus no dynamic request routing is possible.
Finagle ships with a set of predefined schemes you can use.



Inet (DNS)
----------

The ``inet`` scheme uses DNS for resolving network addresses. It's also the default scheme, which means that
if you leave out a ``scheme`` in the location string, the `DnsResolver`_ is used.

.. _DnsResolver: https://github.com/twitter/finagle/blob/develop/finagle-core/src/main/scala/com/twitter/finagle/InetResolver.scala

Scheme
~~~~~~

.. code-block:: bash

    inet!www.scala-lang.org:80

Because ``inet`` is the default scheme you can also write

.. code-block:: bash

    www.scala-lang.org:80

Example
~~~~~~~

.. code-block:: scala

    import com.twitter.finagle.Http
    val client = Http.newService("www.scala-lang.org:80")


Fixed Inet (DNS)
----------------

InetResolver that caches all successful DNS lookups indefinitely and does not poll for updates.
Clients should only use this in scenarios where host -> IP map changes do not occur.

Scheme
~~~~~~

.. code-block:: bash

    fixedinet!www.scala-lang.org:80

Zookeeper (zk)
--------------

.. warning:: The Finagle documentation recommend using the zk2 scheme. See `Zookeeper (zk2)`_

The ``zk`` scheme resolves a `Serverset`_ from the given zookeeper instance and path.

The implementation can be found here: `ZkResolver`_.

Scheme
~~~~~~

.. code-block:: bash

    zk!zookeeper-host!path

.. _ZkResolver: https://github.com/twitter/finagle/blob/develop/finagle-serversets/src/main/scala/com/twitter/finagle/zookeeper/ZkResolver.scala


Example
~~~~~~~

.. code-block:: scala

    import com.twitter.finagle.Http
    val client = Http.newService("zk!127.0.0.1:2181!/path/to/service")


Zookeeper (zk2)
---------------

The ``zk2`` scheme resolves a `Serverset`_ from the given zookeeper instance and path.

The implementation can be found here: `Zk2Resolver`_.

Scheme
~~~~~~

.. code-block:: bash

    zk2!zookeeper-host!path


Example
~~~~~~~

.. code-block:: scala

    import com.twitter.finagle.Http
    val client = Http.newService("zk2!127.0.0.1:2181!/path/to/service")


.. _Zk2Resolver: https://github.com/twitter/finagle/blob/develop/finagle-serversets/src/main/scala/com/twitter/finagle/serverset2/Zk2Resolver.scala


MDNS (mdns)
-----------

TODO

The implementation can be found here: `MDNS`_

.. _MDNS: https://github.com/twitter/finagle/blob/develop/finagle-mdns/src/main/scala/com/twitter/finagle/mdns/MDNS.scala

MDNS (local)
------------

TODO

The implementation can be found here: `Local`_

.. _Local: https://github.com/twitter/finagle/blob/develop/finagle-mdns/src/main/scala/com/twitter/finagle/mdns/Local.scala

JmDNS (jmdns)
-------------

TODO

The implementation can be found here: `JmDNS`_.

.. _JmDNS: https://github.com/twitter/finagle/blob/develop/finagle-mdns/src/main/scala/com/twitter/finagle/mdns/JmDNS.scala


Dtabs - Dynamic Request Routing
===============================

TODO


.. _Zookeeper: https://zookeeper.apache.org/
.. _Serverset: http://twitter.github.io/commons/apidocs/com/twitter/common/zookeeper/ServerSet.html
