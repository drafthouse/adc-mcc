package com.twitter.finatra.http

import com.jakehschwartz.finatra.swagger.FinatraSwagger
import com.twitter.finagle.http.RouteIndex
import io.swagger.models.{Operation, Swagger}

import scala.language.implicitConversions

/**
  * Ignore for assessment.
  *
  * To work around the accessibility of RouteDSL, this class is in "com.twitter.finatra.http" package
  */
object AdcSwaggerRouteDSL {
  implicit def convertToSwaggerRouteDSL(dsl: RouteDSL)(implicit swagger: Swagger): AdcSwaggerRouteDSL = new AdcSwaggerRouteDSLWapper(dsl)(swagger)
}

trait AdcSwaggerRouteDSL {
  implicit protected val swagger: Swagger
  protected val dsl: RouteDSL

  def postWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                                (doc: Operation => Unit)
                                                                (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "post")(doc)
    dsl.post(route, name, admin, routeIndex)(buildCallback("POST", route, callback))
  }

  def getWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                               (doc: Operation => Unit)
                                                               (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "get")(doc)
    dsl.get(route, name, admin, routeIndex)(buildCallback("GET", route, callback))
  }

  def putWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                               (doc: Operation => Unit)
                                                               (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "put")(doc)
    dsl.put(route, name, admin, routeIndex)(buildCallback("PUT", route, callback))
  }

  def patchWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                                 (doc: Operation => Unit)
                                                                 (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "patch")(doc)
    dsl.patch(route, name, admin, routeIndex)(buildCallback("PATCH", route, callback))
  }

  def headWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                                (doc: Operation => Unit)
                                                                (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "head")(doc)
    dsl.head(route, name, admin, routeIndex)(buildCallback("HEAD", route, callback))
  }

  def deleteWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                                  (doc: Operation => Unit)
                                                                  (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "delete")(doc)
    dsl.delete(route, name, admin, routeIndex)(buildCallback("DELETE", route, callback))
  }

  def optionsWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String, name: String = "", admin: Boolean = false, routeIndex: Option[RouteIndex] = None)
                                                                   (doc: Operation => Unit)
                                                                   (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, "options")(doc)
    dsl.options(route, name, admin, routeIndex)(buildCallback("OPTIONS", route, callback))
  }

  def buildCallback[RequestType, ResponseType](verb: String, route: String, callback: RequestType => ResponseType): RequestType => ResponseType =
    (req: RequestType) => operation(verb, route, callback, req)

  def operation[RequestType, ResponseType](verb: String, route: String, callback: RequestType => ResponseType, req: RequestType): ResponseType = {
    callback(req)
  }

  private def registerOperation(path: String, method: String)(doc: Operation => Unit): Unit = {
    val op = new Operation
    doc(op)

    FinatraSwagger.convert(swagger).registerOperation(path, method, op)
  }
}

private class AdcSwaggerRouteDSLWapper(protected val dsl: RouteDSL)(implicit protected val swagger: Swagger) extends AdcSwaggerRouteDSL
