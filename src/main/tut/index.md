---
layout: home
title:  "Home"
section: "home"
technologies:
 - first: ["Scala", "Finagle is written in Scala"]
 - second: ["Finagle", "Finagle is a framework to build resilient and scalable microservices"]
 - third: ["Thrift", "Thrift is binary protocol for RPC"]
---

# Finagle

> Finagle is an extensible RPC system for the JVM, used to construct high-concurrency servers. Finagle implements
> uniform client and server APIs for several protocols, and is designed for high performance and concurrency. Most of
> Finagle’s code is protocol agnostic, simplifying the implementation of new protocols.

This documentation extends the [Finagle Guide](http://twitter.github.io/finagle/guide/), fills the missing gaps and
provides more examples.

# Content

This documentation contains an overview for various Finagle features and provides examples on how to use them.
Finagles solves different problems and provides a lot of abstractions and helpers. We splitted the documentation in
different topics for easier consumption.

## [Finagle Util](/util.html)

The twitter utility library provides a bunch of helpers for standard problems, e.g

- App trait
- Logging
- Flags ( commandline arguments )
- Metrics
- ...

## [Service Discovery](/service-discovery.html)

The service discovery section explains the different options for announcing and disocvering services.
Static and dynamic routing (Dtabs) is also part of this section.


## [Protocols](/protocols.html)

Finagle provides implementations for the http, thrift, thrift mux, mysql, redis and memcached protocols.
We added examples that explain how to implement a server and start a client for these protocols.

## [Contexts](/contexts.html)

Contexts are a Finagle abstraction for implicitly passing request context information along with a request.

# Posts

Here are some resources that are worth reading or watching to get a better undstanding of Finagle.

## Finagle 101

> This post is based on my talk
> [“Finagle: Under the Hood”](http://event.scaladays.org/scaladays-nyc-2016#!#schedulePopupExtras-7565) that was
> presented at Scala Days NYC. I thought I’d publish this for those who prefer reading instead of watching
> (video is not yet published anyway). The full slide deck is available online as well.

Source: [Finagle 101](http://vkostyukov.net/posts/finagle-101/)

## Twitter Scala School

> An introduction to Finagle.

Source: [Twitter Scala School](https://twitter.github.io/scala_school/finagle.html)

## A Beginners Guide for Twitter Finagle

> Twitter Finagle is a great framework to write distributed applications in Scala or Java. This article is a
> “getting started” guide for a simple, distributed application.

Source: [A Beginners Guide for Twitter Finagle](https://medium.com/@muuki88/a-beginners-guide-for-twitter-finagle-7ff7189541e5)

# Videos


<div class="row">
  <div class="col-sm-4">
    <h3>Finagle Under the Hood</h3>
    <iframe width="100%" height="auto" src="https://www.youtube.com/embed/kfs-dtbG0kY?rel=0" frameborder="0" allowfullscreen></iframe>
  </div>
  <div class="col-sm-4">
    <h3>Finagle for Beginners</h3>
    <iframe width="100%" height="auto" src="https://www.youtube.com/embed/zcoE-wq7RF8?rel=0" frameborder="0" allowfullscreen></iframe>
  </div>
  <div class="col-sm-4">
    <h3>Building high performance servers with Twitter</h3>
    <iframe width="100%" height="auto" src="https://www.youtube.com/embed/Ne0BkCtcJa8?rel=0" frameborder="0" allowfullscreen></iframe>
  </div>
</div>
