---
layout: docs
title:  "Announcing"
section: "servicediscovery"
---

# Announcing

Protocols that support implementing a server (like Thrift or Http) usually provide a `serveAndAnnounce` method,
which gives the ability to announce the service under a given *Name*.

The service *Name* string looks similar to the one used for resolving ( see [Static Resolving](/discovery/static-resolving) ):

```
scheme!name
```
