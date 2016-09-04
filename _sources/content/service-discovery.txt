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

Zookeeper
=========

Finagle has first class `Zookeeper`_ support. Both static and dynamic resolving with dtabs make use of zookeeper
to manage service locations. The core data structure for this purpose is the `Serverset`_.

There two schema implementations ``zk`` (used to announce a service) and ``zk2`` ( used to resolve a service ). Both
use the `Serverset`_ internally for persisting, updating or resolving service network locations. Resolving is describe
in more detail in the `Static Resolving`_ section.

.. _Zookeeper: https://zookeeper.apache.org/
.. _Serverset: http://twitter.github.io/commons/apidocs/com/twitter/common/zookeeper/ServerSet.html


Announcing
----------

Protocols that support implementing a server (like Thrift or Http) usually provide a ``serveAndAnnounce`` method,
which gives the ability to announce the service under a given *Name*.

The service *Name* string looks similar to the one used for resolving ( see `Static Resolving` ):

.. code-block:: bash

    schema ! name

For the *Zookeeper* schema the ``name`` parameter can be extend:

.. code-block:: bash

    schema ! host ! path ! shardId

The shardId can always be set to zero. The Finagle documentation says it's only used for a few services
at twitter.


.. code-block:: scala

    val server = ThriftMux.server
      .serveAndAnnounce(
        name = s"zk!127.0.0.1:2181!/path/to/service!0",
        addr = ":8000",
        service = service
      )


Static Resolving
================

TODO


Dtabs - Dynamic Request Routing
===============================

TODO
