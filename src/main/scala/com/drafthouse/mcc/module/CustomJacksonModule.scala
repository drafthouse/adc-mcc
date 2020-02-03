package com.drafthouse.mcc.module

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategy}
import com.jakehschwartz.finatra.swagger.Resolvers
import com.twitter.finatra.json.modules.FinatraJacksonModule

/**
  * Ignore for assessment.
  *
  * This module provides the JacksonModule for the Swagger interface.
  */
object CustomJacksonModule extends FinatraJacksonModule {

  override val serializationInclusion: Include = Include.ALWAYS

  // was: CamelCasePropertyNamingStrategy
  override val propertyNamingStrategy: PropertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE

  // This is overridden so Swagger will render Option[A] as A
  override protected def additionalMapperConfiguration(mapper: ObjectMapper): Unit = {
    super.additionalMapperConfiguration(mapper)
    Resolvers.register(mapper)
  }

}
