package net.gutefrage

import com.twitter.finagle.{Dtab, Filter, Service}
import com.twitter.logging.Logger
import com.twitter.util.Future

class DtabLogger[Req, Rep] extends Filter[Req, Rep, Req, Rep] {

  val log = Logger()

  override def apply(request: Req, service: Service[Req, Rep]): Future[Rep] = {
    log.info(s"Dtab base : ${Dtab.base.show}")
    log.info(s"Dtab local: ${Dtab.local.show}")
    service(request)
  }
}
