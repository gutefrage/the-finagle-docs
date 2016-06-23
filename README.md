# Example Finalge App

The app consists of multiple applications that must be started.

- `TemperatureServer` service with thrift interface to store data and
provide it through a thrift API
- `TemperatureSensor` generates random data and sends it to the
`TemperaetureServer`
- `WeatherApi` vanilla finagle HTTP api to access weather data
- `FinchWeatherApi` HTTP api to access weather data

# Requirements

You need [docker-compose][] and docker to run the mysql and zookeeper
container

[docker-compose](https://docs.docker.com/compose/install/)

# Start the application

```
# start mysql and zookeeper
docker-compose up

# temperature server
sbt "runMain net.gutefrage.TemperatureServer --protocol mux --port 8081"

# sensor sensor
sbt "runMain net.gutefrage.TemperatureSensor --protocol mux"

# http api
sbt "runMain net.gutefrage.FinchWeatherApi --port 8000"

# ask for the mean temperature
curl localhost:8000/weather/mean
```
