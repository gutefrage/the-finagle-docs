package net.gutefrage.filter

import com.twitter.finagle.{Dtab, Service, SimpleFilter}
import com.twitter.util.Future
import org.slf4j.LoggerFactory

/**
  * Logging dtabs for each request so we can examine the dtabs
  *
  * @tparam Req
  * @tparam Rep
  */
class DtabLogger[Req, Rep] extends SimpleFilter[Req, Rep] {

  val log = LoggerFactory.getLogger("dtab")

  override def apply(request: Req, service: Service[Req, Rep]): Future[Rep] = {
    log.debug(s"Dtab base : ${Dtab.base.show}")
    log.debug(s"Dtab local: ${Dtab.local.show}")
    service(request)
  }
}
