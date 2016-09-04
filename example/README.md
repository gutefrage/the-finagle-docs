# Example Finagle Applications

This example project contains various implementations of the same services.
Each example demonstrates a different finagle feature. 

The services are in different packages. Some basic utilities are
in the top-level package `net.gutefrage`.

## Requirements

You need [docker-compose][] and docker to run the zookeeper, redis and
mysql containers.

[docker-compose](https://docs.docker.com/compose/install/)

Start them with

```
docker-compose up
```

## Starting a service

Each service has some basic parameters you should set

* `-port`: services binds to this port
  Example `-port 8080`
* `-env`: environment to start. `local` and `prod` are available. This necessary to demonstrate some features.
  Example `-env local`
* `admin.port`: used by the TwitterServer implementation to provide an admin interface. The interface is available
  at `localhost:<admin.port>/admin`
  Example: `-admin.port :9081`

An example service start looks like this

```
sbt "runMain net.gutefrage.basic.TemperatureServer -port 8081 -env local -admin.port :9081"
```

**basic** is the feature package. You can use sbts auto-completion feature to discover available
main classes 

```
sbt
> run net
(press tab-tab)
> runMain net.gutefrage.
  net.gutefrage.basic.TemperatureSensorDtabs    net.gutefrage.basic.TemperatureSensorStatic   net.gutefrage.basic.TemperatureServer
  net.gutefrage.basic.WeatherApi                net.gutefrage.context.TemperatureServer       net.gutefrage.context.WeatherApi 
```

# Example Services

## Basic

This packages contains a complete system implemented in a very simplistic way. The `TemperatureServer`
and `WeatherApi` come in a single version. The _sensor_ is implemented with two different client
resolving techniques

- Static resolving 
- Dynamic resolving per request with Dtabs

To start the complete system


```
# start mysql and zookeeper
docker-compose up

sbt "runMain net.gutefrage.basic.TemperatureServer -port 8081 -env local -admin.port :9081"
sbt "runMain net.gutefrage.basic.TemperatureServer -port 8082 -env prod -admin.port :9082"

# sensor sending to local. Static client resolving
sbt "runMain net.gutefrage.basic.TemperatureSensorStatic -env local -admin.port :9181"

# sensor sending to prod by routing request via dtabs
sbt "runMain net.gutefrage.basic.TemperatureSensorDtabs -dtab /env=>/s#/prod -admin.port :9182"

# http api
sbt "runMain net.gutefrage.basic.WeatherApi -port 8000 -admin.port :9080"

# ask for the mean temperature
curl localhost:8000/weather/mean
```

Route your requests to another environment

```bash
curl --request GET \
  --url http://localhost:8000/weather/mean \
  --header 'dtab-local: /env => /s#/prod' 
```

### Admin UI

Each service has its own admin interface. 

| Service | URL                  |
| ------- | -------------------- |
| Server (local)  | http://localhost:9081/admin |
| Server (prod)   | http://localhost:9082/admin |
| Sensor (static) | http://localhost:9181/admin |
| Sensor (dtab)   | http://localhost:9182/admin |
| Http API        | http://localhost:9080/admin |

You can change the ports with the `admin.port` flag.


## Context

TODO

## Filter

TODO
