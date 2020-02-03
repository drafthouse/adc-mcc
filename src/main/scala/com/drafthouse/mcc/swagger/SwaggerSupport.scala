package com.drafthouse.mcc.swagger

import com.jakehschwartz.finatra.swagger.{FinatraOperation, FinatraSwagger}
import com.twitter.finatra.http.{AdcSwaggerRouteDSL, Controller, SwaggerRouteDSL}

/**
  * Ignore for assessment
  */
trait SwaggerSupport extends AdcSwaggerRouteDSL {
  self: Controller =>
  override protected val dsl = self

  implicit protected val convertToFinatraOperation = FinatraOperation.convert _
  implicit protected val convertToFinatraSwagger = FinatraSwagger.convert _
  implicit protected val convertToSwaggerRouteDSL = SwaggerRouteDSL.convert _
}
