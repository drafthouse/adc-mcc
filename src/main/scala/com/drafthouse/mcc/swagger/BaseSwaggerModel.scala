package com.drafthouse.mcc.swagger

import com.drafthouse.mcc.domain.Hardcoded
import io.swagger.models.{Info, Swagger, Tag}

import scala.collection.JavaConverters._

/** Top-level configuration of the Swagger model. Used for ordering the tags and for top-level documentation */
object BaseSwaggerModel extends Swagger {
  info(new Info()
    .description(
      s"""
        | This server hosts the APIs implemented as part of the Alamo Drafthouse Middleware Code Assessment (MCA).
        |
        | The server is provided as a starting framework with example API endpoints, services, and supporting
        | classes to jump start the implementation and to provide some guidance as to how to write an end-to-end
        | middleware service in an Alamo-like Finatra/Finagle-based environment utilizing Circe and Swagger.
        |
        | # API Organization
        | The API is broken into a couple of different sections to make it easier to find the API that you
        | are looking for.
        |
        | * **${Hardcoded.ApiTags.SAMPLE}** - Sample endpoints that come out of the box. Read the implementations
        | and follow the patterns in the assessment implementation.
        | * **${Hardcoded.ApiTags.ASSESSMENT}** - Endpoints required as part of the assessment implementation.
      """.stripMargin)
    .version("0.1.0-SNAPSHOT")
    .title("Middleware Coding Assessment Server"))
    .tags(List(
      new Tag().name(Hardcoded.ApiTags.SAMPLE),
      new Tag().name(Hardcoded.ApiTags.ASSESSMENT)
    ).asJava)
}
