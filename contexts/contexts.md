# Contexts

The [Finagle documentation on Contexts] gives a good introduction and describes multiple use cases. They can be used
to transport request meta data or application-wide context like authentication data.

From the Finagle documentation:

> Contexts can be either local or broadcast. Local contexts do not cross process boundaries while broadcast
> contexts may be marshalled and transmitted across process boundaries.

## Custom Contexts

This section demonstrates how to create a custom `Context` and how to use them. As an example we will provide a
basic authentication information as context.

```scala
case class UserContext(userId: Long)
```

A context is described and typed by a [Key]. The [Contexts] object provides the necessary context instances to create
a Key.

### Local Context

Local contexts are easy to create, but are limited to the process. No marshalling is required.

```scala
import com.twitter.finagle.context.Contexts

object UserContextLocal extends Contexts.local.Key[UserContext] {
  /**
    * @return The local UserContext if set
    */
  def current: Option[UserContext] = Contexts.local.get(UserContextLocal)
}
```

Now it's possible to access the `UserContext` in your service at any place in the request chain with
`UserContextLocal.current()`.

### Broadcast Context

Broadcast contexts get serialized and are available across service boundaries. For this reason you must

1. Implement marshalling and unmarshalling methods
2. Provide a string identifier for the context so that Finagle can look it up. Normally the fully qualified class name
   is used

A low level implementation with `com.twitter.io.Buf` looks like this.

```scala
import com.twitter.finagle.context.Contexts
import com.twitter.finagle.util.ByteArrays
import com.twitter.io.Buf
import com.twitter.util.{Return, Throw, Try}

object UserContext extends
  Contexts.broadcast.Key[UserContext]("net.gutefrage.context.UserContext") {

  private val bodyLengthBytes = 8

  /**
    * Returns the current request's UserContext, if set.
    */
  def current: Option[UserContext] = Contexts.broadcast.get(UserContext)

  override def marshal(userContext: UserContext): Buf = {
    val bytes = new Array[Byte](bodyLengthBytes)
    ByteArrays.put64be(bytes, 0, userContext.userId)
    Buf.ByteArray.Owned(bytes)
  }

  override def tryUnmarshal(body: Buf): Try[UserContext] = {
    if (body.length != bodyLengthBytes) {
      return Throw(new IllegalArgumentException(
        s"Invalid body. Length ${body.length} but required 16"
      ))
    }

    val bytes = Buf.ByteArray.Owned.extract(body)
    val userId = ByteArrays.get64be(bytes, 0)

    Return(UserContext(userId))
  }
}
```

### Usage

Setting a context works the same way for both types with the only difference that you choose either `Contexts.local`
or `Contexts.broadcast`.

```scala
import com.twitter.finagle.context.Contexts

// create an actual context
val userContext = UserContext(5L)

// set the context for all requests made in the given block
Contexts.broadcast.let(UserContext, userContext) {
  // the `aMethod` in `someService` will be able to access
  // this UserContext with UserContext.current()
  someService.aMethod()
}
```

[Finagle documentation on Contexts]: https://twitter.github.io/finagle/guide/Contexts.html
[Contexts]: https://twitter.github.io/finagle/docs/#com.twitter.finagle.context.Contexts$
[Key]: https://twitter.github.io/finagle/docs/index.html#com.twitter.finagle.context.Context@Key[A]
