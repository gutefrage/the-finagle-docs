package net.gutefrage.context

import com.twitter.finagle.context.Contexts
import com.twitter.finagle.util.ByteArrays
import com.twitter.io.Buf
import com.twitter.util.{Return, Throw, Try}

case class UserContext(userId: Long)

/**
  * @see [[com.twitter.finagle.Deadline]]
  * @see [[com.twitter.finagle.thrift.ClientId]]
  * @see https://twitter.github.io/finagle/guide/Contexts.html
  */
object UserContext extends Contexts.broadcast.Key[UserContext]("net.gutefrage.context.UserContext"){

  private val bodyLengthBytes = 8

  /**
    * Returns the current request's deadline, if set.
    */
  def current: Option[UserContext] = Contexts.broadcast.get(UserContext)

  override def marshal(userContext: UserContext): Buf = {
    val bytes = new Array[Byte](bodyLengthBytes)
    ByteArrays.put64be(bytes, 0, userContext.userId)
    Buf.ByteArray.Owned(bytes)
  }

  override def tryUnmarshal(body: Buf): Try[UserContext] = {
    if (body.length != bodyLengthBytes) {
      return Throw(new IllegalArgumentException(s"Invalid body. Length ${body.length} but required 16"))
    }

    val bytes = Buf.ByteArray.Owned.extract(body)
    val userId = ByteArrays.get64be(bytes, 0)

    Return(UserContext(userId))
  }
}
