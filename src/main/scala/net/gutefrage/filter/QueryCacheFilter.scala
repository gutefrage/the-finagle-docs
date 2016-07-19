package net.gutefrage.filter

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.redis._
import serialization._
import Parse.Implicits._
import net.gutefrage.util.ThriftByteArray._

class QueryCacheFilter(val methodsToCache: Option[Seq[String]] = None, redisClient: RedisClient) extends SimpleFilter[Array[Byte], Array[Byte]] {

  def bytesToInt(bytes: Array[Byte]): Int = java.nio.ByteBuffer.wrap(bytes).getInt

  def apply(request: Array[Byte], service: Service[Array[Byte], Array[Byte]]): Future[Array[Byte]] = {
    val (methodName, seqId) = request.binaryProtocolMethodNameSeqId

    if (methodsToCache.isEmpty || methodsToCache.get.contains(methodName)) {
      println(s"Incoming request with method: $methodName -> Try to serve it from cache.")
      val redisKey = request.binaryProtocolChangeSeqId(Array[Byte](0, 0, 0, 0)).requestHashKey

      redisClient.get[Array[Byte]](redisKey) match {
        case Some(response) => {
          println("Data in redis found, returning from cache")
          Future.value(response)
        }
        case None => {
          println("data not in cache yet")
          service(request) map { result =>
            redisClient.setex(redisKey, 15, result)
            result
          }
        }
      }

    } else {
      println(s"Incoming request with method: $methodName -> Don't serve it from cache.")
      service(request)
    }

  }

}
