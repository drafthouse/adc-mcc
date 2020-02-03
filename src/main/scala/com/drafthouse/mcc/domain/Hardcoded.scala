package com.drafthouse.mcc.domain

import java.time.LocalTime

/**
  * Central location for tracking hardcoded/"magic" values
  */
object Hardcoded {

  object DateTime {
    val BUSINESS_DAY_START_TIME_CLT = LocalTime.of(6, 0)
  }

  object Market {
    val NATIONAL_MARKET_SLUG = "national"
    val NATIONAL_MARKET_ID = "9900"
  }

  /** API Tags for Swagger. */
  object ApiTags {
    val SAMPLE = "Sample"
    val ASSESSMENT = "Assessment"
  }

}
