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

sbt "runMain net.gutefrage.TemperatureServer -port 8081 -env local -admin.port :9081"
sbt "runMain net.gutefrage.TemperatureServer -port 8082 -env prod -admin.port :9082"

# sensor sensor
sbt "runMain net.gutefrage.TemperatureSensor"

# http api
sbt "runMain net.gutefrage.WeatherApi -port 8000"

# ask for the mean temperature
curl localhost:8000/weather/mean
```

Route your requests to another environment

```bash
curl --request GET \
  --url http://localhost:8000/weather/mean \
  --header 'dtab-local: /env => /s#/prod' 
```
