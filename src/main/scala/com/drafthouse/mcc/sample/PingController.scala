package com.drafthouse.mcc.sample

import javax.inject.{Inject, Singleton}
import com.drafthouse.mcc.core.AdcBaseController
import com.drafthouse.mcc.domain.Hardcoded
import com.drafthouse.mcc.sample.PingController.{PongData, PongPayload}
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import com.typesafe.config.Config
import io.swagger.annotations.ApiModelProperty
import io.circe.generic.auto._
import io.circe.syntax._

/** Example controller with Swagger documentation and some basic Circe-based serialization */
@Singleton
class PingController @Inject()
(
  config: Config // Most commonly, dependencies are injected by type
) extends AdcBaseController { // Controllers should extend AdcBaseController for Swagger, logging, and service prefix

  val reply = config.getString("adc.feature.ping.reply") // e.g. (Required) config entries can be read from application.conf

  // e.g. Controllers surface endpoints using getWithMETHOD methods
  getWithDoc(s"${servicePrefix}/sample/ping") { doc =>
    doc.summary("Returns a simple response")
      .description(
        """
          | Endpoints should provide extended documentation describing how they should be used and
          | expected errors that could be returned. The `description` section of the Swagger
          | documentation is used to provide that level of documentation.
        """.stripMargin
      )
      .tag(Hardcoded.ApiTags.SAMPLE)
      .responseWith[PongPayload](200, "The reply")
  } { _: Request => // Controller methods take a Request and answer a Future[Response]

    for {
      _ <- Future.Unit // Typically we'll be doing something asynchronously. We prime the standard pattern with an ignored Future.
      _ = info(s"Invoking ping. Responding with ${reply}") // the Logging trait provides debug, info, warn, and error methods for logging
      payload = PongPayload(PongData(reply)) // Case classes can be automatically serialized by Circe
      json = payload.asJson.noSpaces // Some Circe magic (import generic.auto._ and syntax._)
      resp = response.ok.json(json) // Build the response with the inherited ResponseBuilder (response)
    } yield resp

  }

}

object PingController {

  case class PongData
  (
    @ApiModelProperty(value = "The reply", required = true) reply: String
  )
  case class PongPayload
  (
    @ApiModelProperty(required = true) data: PongData
  )

}
