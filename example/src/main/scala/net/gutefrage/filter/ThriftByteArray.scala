package net.gutefrage.filter

/** Credit: http://stevenskelton.ca/finagle-query-cache-with-guava/ */



import com.google.common.hash.Hashing
import org.apache.commons.codec.binary.Base64

class ThriftByteArray(requestOrResponse: Array[Byte]) {
  def bytesToInt(bytes: Array[Byte]): Int = java.nio.ByteBuffer.wrap(bytes).getInt

  def requestHashKey: String = Base64.encodeBase64String(Hashing.md5.hashBytes(requestOrResponse).asBytes)

  def binaryProtocolMethodNameSeqId: (String, Array[Byte]) = {
    val methodNameLength = bytesToInt(requestOrResponse.slice(4, 8))
    val methodName = new String(requestOrResponse.slice(8, 8 + methodNameLength), "UTF-8")
    val seqId = requestOrResponse.slice(8 + methodNameLength, 12 + methodNameLength)
    (methodName, seqId)
  }

  def binaryProtocolChangeSeqId(seqId: Array[Byte] = Array(0, 0, 0, 0)): Array[Byte] = {
    val methodNameLength = bytesToInt(requestOrResponse.slice(4, 8))
    val retVal = new Array[Byte](requestOrResponse.length)
    requestOrResponse.copyToArray(retVal)
    seqId.copyToArray(retVal, 8 + methodNameLength, 4)
    retVal
  }

  /** Non-zero status is a thrown error */
  def binaryResponseExitStatus: Byte = {
    val methodNameLength = bytesToInt(requestOrResponse.slice(4, 8))
    //skip over: version,methodLength,method,seqId,msgType,successStruct
    val positionMessageType = 4 + 4 + methodNameLength + 4 + 1 + 1
    requestOrResponse(positionMessageType)
  }


}

object ThriftByteArray {
  implicit def enrichArray(xs: Array[Byte]) = new ThriftByteArray(xs)
}
