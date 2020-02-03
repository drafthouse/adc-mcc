package com.drafthouse.mcc.controller

import java.time.{DayOfWeek, LocalDateTime, ZoneOffset}

import com.drafthouse.mcc.controller.FillRateController.{FillRateByDayOfWeek, FillRateByDayPart, FillRatePayload, FillRateReport}
import com.drafthouse.mcc.core.AdcBaseController
import com.drafthouse.mcc.domain.{DayPart, Hardcoded}
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import com.typesafe.config.Config
import io.circe.generic.auto._
import io.circe.syntax._
import com.drafthouse.mcc.core.CommonCirceSerializers._
import io.swagger.annotations.ApiModelProperty
import javax.inject.{Inject, Singleton}

/**
  * The controller for the fill rate endpoint required by the coding challenge. This is a potential starting
  * point but create whatever API makes the most sense to you (one or more endpoints)
  */
@Singleton
class FillRateController @Inject()
(
  config: Config
) extends AdcBaseController {

  getWithDoc(s"${servicePrefix}/v1/session-fill-rate") { doc =>
    doc.summary("Returns a summary of the fill rate for the given constraints")
      .description(
        """
          | A placeholder for the documentation of the fill rate endpoint. Add your documentation here.
        """.stripMargin
      )
      .tag(Hardcoded.ApiTags.ASSESSMENT)
      .responseWith[FillRatePayload](200, "The fill rate report")
  } { _: Request =>

    for {
      _ <- Future.Unit // Typically we'll be relying on asynchronous services. We prime the standard pattern with an ignored Future for the purposes of the (synchronous) stub.
      payload = FillRatePayload(FillRateReport(
        startTimeUtc = LocalDateTime.now(ZoneOffset.UTC),
        endTimeUtc = LocalDateTime.now(ZoneOffset.UTC),
        marketSlugs = List.empty,
        cinemaSlugs = List.empty,
        filmSlugs = List.empty,
        seriesSlugs = List.empty,
        fillRateOverallPercentHundredths = 2004,
        fillRateByDayOfWeek = List(
          FillRateByDayOfWeek(
            dayOfWeekClt = DayOfWeek.SATURDAY,
            fillRateOverallPercentHundredths = 2000,
            fillRateByDayPartClt = List(
              FillRateByDayPart(
                dayPartClt = DayPart.MATINEE,
                fillRatePercentHundredths = 1450
              ),
              FillRateByDayPart(
                dayPartClt = DayPart.PRIME,
                fillRatePercentHundredths = 5560
              )
            )
          )
        ),
        fillRateByDayPart = List(
          FillRateByDayPart(
            dayPartClt = DayPart.MATINEE,
            fillRatePercentHundredths = 1550
          ),
          FillRateByDayPart(
            dayPartClt = DayPart.PRIME,
            fillRatePercentHundredths = 5260
          )
        )
      ))
      json = payload.asJson.noSpaces
      resp = response.ok.json(json)
    } yield resp

  }

}

object FillRateController {

  case class FillRatePayload
  (
    @ApiModelProperty(value = "The fill rate report", required = true) data: FillRateReport
  )
  case class FillRateReport
  (
    @ApiModelProperty(value = "The time of the first session included in the report in UTC", required = true) startTimeUtc: LocalDateTime,
    @ApiModelProperty(value = "The time of the last session included in the report in UTC", required = true) endTimeUtc: LocalDateTime,
    @ApiModelProperty(value = "The slugs of the markets included in the report", required = true) marketSlugs: List[String],
    @ApiModelProperty(value = "The slugs of the cinemas included in the report", required = true) cinemaSlugs: List[String],
    @ApiModelProperty(value = "The slugs of the films included in the report", required = true) filmSlugs: List[String],
    @ApiModelProperty(value = "The slugs of the series included in the report", required = true) seriesSlugs: List[String],
    @ApiModelProperty(value = "The overall fill rate", required = true) fillRateOverallPercentHundredths: Int,
    @ApiModelProperty(value = "The fill rates by day of week", required = true) fillRateByDayOfWeek: List[FillRateByDayOfWeek],
    @ApiModelProperty(value = "The overall fill rate by day part", required = true) fillRateByDayPart: List[FillRateByDayPart]
  )
  case class FillRateByDayOfWeek
  (
    @ApiModelProperty(value = "The business day of the week (CLT)", required = true) dayOfWeekClt: DayOfWeek,
    @ApiModelProperty(value = "The business day's overall fill rate in hundredths of a percent", required = true) fillRateOverallPercentHundredths: Int,
    @ApiModelProperty(value = "The fill rate by day part (CLT)", required = true) fillRateByDayPartClt: List[FillRateByDayPart]
  )
  case class FillRateByDayPart
  (
    @ApiModelProperty(value = "The part of the day in cinema-local time (CLT)", required = true) dayPartClt: DayPart,
    @ApiModelProperty(value = "The fill rate in hundredths of a percent", required = true) fillRatePercentHundredths: Int
  )

}