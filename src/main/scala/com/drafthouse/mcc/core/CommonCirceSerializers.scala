package com.drafthouse.mcc.core

import java.time.{DayOfWeek, LocalDateTime}
import java.time.format.DateTimeFormatter

import cats.syntax.either._
import com.drafthouse.mcc.domain.DayPart
import com.twitter.inject.Logging
import io.circe.{Decoder, Encoder}

/** Circe serializers for commonly used types and non-case class domain types */
object CommonCirceSerializers extends Logging {

  // ---- LocalDateTime -------------
  implicit val decodeLocalDateTime: Decoder[LocalDateTime] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
      .leftMap(t => s"Failed to parse LocalDateTime: ${str}")
  }

  implicit val encodeLocalDateTime: Encoder[LocalDateTime] = Encoder.encodeString.contramap[LocalDateTime](DateTimeFormatter.ISO_LOCAL_DATE_TIME.format)

  // ---- DayOfWeek -------------
  implicit val decodeDayOfWeek: Decoder[DayOfWeek] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(DayOfWeek.valueOf(str.toUpperCase))
      .leftMap(t => s"Unable to parse DayOfWeek: ${str}")
  }
  implicit val encodeDayOfWeek: Encoder[DayOfWeek] = Encoder.encodeString.contramap[DayOfWeek](_.toString)

  // ---- DayPart -------------
  implicit val decodeDayPart: Decoder[DayPart] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(try {
      DayPart.valueOf(str.toUpperCase)
    } catch {
      case e: IllegalArgumentException => DayPart.UNKNOWN
    }).leftMap(t => s"Unable to parse DayPart: ${str}")
  }
  implicit val encodeDayPart: Encoder[DayPart] = Encoder.encodeString.contramap[DayPart](_.toString)

}
