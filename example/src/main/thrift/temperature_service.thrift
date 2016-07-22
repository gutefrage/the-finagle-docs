namespace * net.gutefrage.temperature.thrift

/**
 * temperature in celisus and timestamp in UTC milliseconds
 */
struct TemperatureDatum {
  1: i32 celsius,
  2: i64 timestamp
}


service TemperatureService {
   void add(1: TemperatureDatum datum);

   double mean();
}
