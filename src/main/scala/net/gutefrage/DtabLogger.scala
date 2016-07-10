package net.gutefrage

import com.twitter.finagle.{Dtab, Filter, Service}
import com.twitter.util.Future
import org.slf4j.LoggerFactory

class DtabLogger[Req, Rep] extends Filter[Req, Rep, Req, Rep] {

  val log = LoggerFactory.getLogger("dtab")

  override def apply(request: Req, service: Service[Req, Rep]): Future[Rep] = {
    log.info(s"Dtab base : ${Dtab.base.show}")
    log.info(s"Dtab local: ${Dtab.local.show}")
    service(request)
  }
}
