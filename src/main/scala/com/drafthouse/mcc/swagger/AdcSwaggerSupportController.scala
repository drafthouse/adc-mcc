/**
  * Ignore for assessment.
  *
  * Copied from https://github.com/xiaodongw/swagger-finatra/blob/master/src/main/scala/com/github/xiaodongw/swagger/finatra/SwaggerController.scala
  * Overridden to support serviceRoot and better support docPath
  */
package com.drafthouse.mcc.swagger

import javax.inject.Singleton

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache
import io.swagger.models.Swagger
import io.swagger.util.Json

@Mustache("index")
case class SwaggerView(title: String, path: String)

@Singleton
class AdcSwaggerController(serviceRoot: String = "", docPath: String = "/api-docs", swagger: Swagger) extends Controller {
  get(s"${docPath}/model") { request: Request =>
    response.ok.body(Json.mapper.writeValueAsString(swagger))
      .contentType("application/json").toFuture
  }
  
  get(s"${docPath}/ui") { request: Request =>
    response.temporaryRedirect
      .location(serviceRoot + "/webjars/swagger-ui/2.2.10/index.html?url=" + docPath + "/model")
  }
}