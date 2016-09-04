package net.gutefrage

import com.twitter.finagle.Dtab

/**
  * Holds different delegation tables
  */
object Dtabs {

  def init(additionalDtab: Dtab = Dtab.empty): Unit = {
    Dtab.base = base ++ additionalDtab
  }

  /**
    * base dtab to resolve requests on your local system
    */
  val base = Dtab.read(
    """|/zk##    => /$/com.twitter.serverset;
       |/zk#     => /zk##/127.0.0.1:2181;
       |/s#      => /zk#/service;
       |/env     => /s#/local;
       |/s       => /env;""".stripMargin)
}
