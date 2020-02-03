package com.drafthouse.mcc.core

import com.drafthouse.mcc.swagger.{BaseSwaggerModel, SwaggerSupport}
import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import io.swagger.models.Swagger

abstract class AdcBaseController extends Controller with SwaggerSupport with Logging {

  implicit protected val swagger: Swagger = BaseSwaggerModel

  /** The root path for the services. No trailing slash. Root is empty string */
  protected val servicePrefix: String = BootConfig.servicePrefix

}
