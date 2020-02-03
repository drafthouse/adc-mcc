package com.drafthouse.mcc

import com.drafthouse.mcc.AdcMccServerMain.CorsPreflightController
import com.drafthouse.mcc.controller.FillRateController
import com.drafthouse.mcc.core.{AdcExceptionMapper, BootConfig, GlobalRedirectController}
import com.drafthouse.mcc.module._
import com.drafthouse.mcc.sample.{PingController, SampleFeedController}
import com.drafthouse.mcc.swagger.{AdcSwaggerController, AdcWebjarsController, BaseSwaggerModel}
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.inject.TwitterModule

/**
  * This is the bootstrap class for the HTTP Server.
  */
class AdcMccServer extends HttpServer {

  // The JVM has an infinite cache on successful DNS resolution. This limits it to one minute in case DNS entries change
  java.security.Security.setProperty("networkaddress.cache.ttl", "60")

  override def modules: Seq[TwitterModule] = Seq(TypesafeConfigModule) ++ HttpClientModules.modules

  override def jacksonModule: TwitterModule = CustomJacksonModule

  override def defaultFinatraHttpPort = ":9999"

  override def configureHttp(router: HttpRouter) {

    val servicePrefix = BootConfig.servicePrefix

    // Hosted Swagger UI needs to be able to pull API. We let anyone access the API docs
    val corsFilter = new Cors.HttpFilter(Cors.UnsafePermissivePolicy)

    router
      .exceptionMapper[AdcExceptionMapper]

      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter(corsFilter)
      .filter[CommonFilters] // This contains ExceptionMappingFilter. Make sure it's last or it will skip other filters on errors

      // Swagger support
      .add[AdcWebjarsController]
      .add(corsFilter, new AdcSwaggerController(servicePrefix, swagger = BaseSwaggerModel, docPath = s"${servicePrefix}/api-docs"))

      .add[GlobalRedirectController]

      // Sample
      .add[PingController]
      .add[SampleFeedController]

      // Assessment. !!! Add your assessment controllers here !!!
      .add[FillRateController]

      // CORS preflight. Handle last for catch-all OPTIONS requests
      .add[CorsPreflightController]
  }

}

object AdcMccServerMain extends AdcMccServer {

  class CorsPreflightController() extends Controller {
    options("/:*") {
      _: Request => response.ok
    }
  }

}
