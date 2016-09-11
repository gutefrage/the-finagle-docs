Thrift & Thrift Mux
===================

.. caution:: This section is under construction.


What is Thrift
--------------

  "Apache Thrift allows you to define data types and service interfaces in a simple definition file.
  Taking that file as input, the compiler generates code to be used to easily build RPC clients and
  servers that communicate seamlessly across programming languages. Instead of writing a load of
  boilerplate code to serialize and transport your objects and invoke remote methods, you can get
  right down to business." - `Apache Thrift`_


Finagle provides its own code generator `Scrooge`_ to generate Thrift encoders and decoders, but also
the necessary finagle boilerplate create server and clients. The `Scrooge Code Generator`_ section
describes the setup in more detail.


Thrift vs ThriftMux
-------------------

Finagle provides two thrift protocol implementations. A standard ``Thrift`` protocol that is
compatible with other Thrift server and clients. And an improved ``ThriftMux`` implementation
that allows multiplexing one single connections.

If you have a heterogeneous environment the standard ``Thrift`` protocol should be used. For
pure finagle environments the ``ThriftMux`` protocol is recommended as it provides better
performance while using less resources.

.. note:: The list of differences is not complete.

Distribute Thrift Definitions
-----------------------------

A major benefit when using thrift is that you don't have to distribute binaries ( class files or actual code ),
but language agnostic thrift files. How you distribute these files depends on your service structure,
build process and infrastructure.

At gutefrage every service contains two sbt submodules:

- ``server`` - the actual implementation of the service
- ``api`` - a package containing the thrift files and the service name

Depending on a service means depending on the ``api`` package. Each service generates the necessary
code with Scrooge to instantiate service clients. The next section describes the Scrooge setup.

Scrooge Code Generator
----------------------

`Scrooge`_ generates the necessary services stubs and finagle boilerplate to implement and
serve thrift services. Activate it by adding this to your ``plugins.sbt``:

.. code-block:: scala

    resolvers += "twitter-repo" at "https://maven.twttr.com"

    addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "4.9.0")

Put your thrift files in ``src/main/thrift``. The thrift sbt-plugin will pick the up and
generate the code when you run ``sbt compile``.


Include Thrift Files
~~~~~~~~~~~~~~~~~~~~

Scrooge can extract ``thrift`` files from dependencies when added to the ``scroogeThriftDependencies``
setting. This autoplugin adds ``thrift`` files to the created jar.

.. code-block:: scala

    /**
     * This plugin adds the thrift files to the packaged jar.
     */
    object ApiThriftSourcesPlugin extends AutoPlugin {

      override def requires = GFApiPlugin

      override def trigger = AllRequirements

      override def projectSettings: Seq[Setting[_]] = Seq(
        // add thrift files to output jar
        mappings in (Compile, packageBin) ++= {
          (sourceDirectory.value / "main" / "thrift").listFiles()
            .filter(f => f.getName endsWith ".thrift")
            .map { file =>
              file -> (s"com/yourcompany/server/<servicename>/thrift/${file.getName}")
            }
            .toSeq
        }
      )
    }


Add Thrift Dependencies automatically
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This auto plugin provides a basic way to add ``libraryDependencies`` coming from a certain
organization to the ``scroogeThriftDependencies``. Scrooge will then extract the ``thrift``
files from these dependencies and process them.

.. code-block:: scala

    import sbt._
    import com.twitter.scrooge.ScroogeSBT

    object AddThriftDependencies extends AutoPlugin {

      override def requires = ScroogeSBT
      override def trigger = AllRequirements

      override def projectSettings = Seq(
        // add library dependencies to scrooge
        scroogeThriftDependencies in Compile := {
          // splits (2.10)(.x)
          val pattern = "(\\d*\\.{1}\\d*)(.*)".r
          val pattern(version, _) = scalaVersion.value

          // adds all services in library dependencies as thrift dependencies
          libraryDependencies.value
            .filter(_.organization startsWith "com.yourcompany.service")
            .map(_.name + "_" + version)
        }
      )
    }


Server
------



Client
------

.. _Scrooge: https://scrooge
.. _Apache Thrift: https://thrift.apache.org/
