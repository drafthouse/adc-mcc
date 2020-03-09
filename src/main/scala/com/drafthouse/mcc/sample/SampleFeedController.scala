package com.drafthouse.mcc.sample

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

import com.drafthouse.mcc.core.AdcBaseController
import com.drafthouse.mcc.domain.{AdcBadRequestError, AdcError, Hardcoded}
import com.drafthouse.mcc.sample.SampleFeedController.{FeedFilmSerialization, FeedFilmsData, FeedFilmsPayload, MarketSerialization}
import com.drafthouse.mcc.sample.SampleFeedService.{FeedFilm, FeedMarket}
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import com.typesafe.config.Config
import io.swagger.annotations.ApiParam
import javax.inject.{Inject, Singleton}
import io.circe.generic.auto._
import io.circe.syntax._
import io.swagger.models.parameters.{PathParameter, QueryParameter}
import org.apache.commons.lang3.StringUtils

@Singleton
class SampleFeedController @Inject()
(
  feedService: SampleFeedService,
  config: Config
) extends AdcBaseController {

  getWithDoc(s"${servicePrefix}/sample/market/:marketIdOrSlug/film") { doc =>
    doc.summary("Answers limited information about the films from the market feed")
      .description(
        s"""
          | Answers the basic identifying information about the films that are currently
          | on sale in the specified market. The market can be specified using either the market slug
          | or the 4-digit ID of the market. No aggregation is done for the "national" market
          | as this represents only the data directly available from the feed.
          |
          | Expected Errors:
          |
          | * ${AdcError.HttpNotFound.toErrorDocumentation} - If the market or feed can't be found
        """.stripMargin
      )
      .tag(Hardcoded.ApiTags.SAMPLE)
      .parameter(new PathParameter()  // There are PathParameters and QueryParameters
        .name("marketIdOrSlug")
        .description("The 4-digit Vista market ID")
        .example("2100")
        .`type`("string"))
      .responseWith[FeedFilmsPayload](200, "The films on sale in the market")
      .responseWith(400, "The parameters are invalid")
      .responseWith(404, "The specified market does not exist")
      .responseWith(500, "Server error or connection error to back-end servers")
  } { req: Request =>

    // Process each of the parameters and the payload and perform basic validation on each
    val eMarketIdOrSlug = req.params.get("marketIdOrSlug")
      .map(s => URLDecoder.decode(s, StandardCharsets.UTF_8.name))
      .flatMap(s => if (StringUtils.isBlank(s)) None else Option(Right(StringUtils.trim(s))))
      .getOrElse(Left(List("marketIdOrSlug is required")))

    // Build a tuple of the parameters/payload. If they are all valid, perform any
    // cross parameter validation and consolidate into a single Either
    val eValidatedRequest = (eMarketIdOrSlug) match {
      case (Right(marketIdOrSlug)) => Right(marketIdOrSlug)
      case _ => Left(List(eMarketIdOrSlug).flatMap(_.left.getOrElse(List.empty)))
    }

    // Service the request with either a 400 if there are parameter issues or with the implementation
    eValidatedRequest match {
      case Left(validationMessages) => Future.exception(AdcBadRequestError(validationMessages))
      case Right(marketIdOrSlug) => for {
        (rawMarket, rawFilms) <- feedService.fetchFeedMarketAndFilms(marketIdOrSlug)
        market = MarketSerialization.fromFeedMarket(rawMarket)
        films = rawFilms.map(FeedFilmSerialization.fromFeedFilm)
        payload = FeedFilmsPayload(FeedFilmsData(market = market, films = films))
        json = payload.asJson.noSpaces
        resp = response.ok.json(json)
      } yield resp
    }
  }

}

object SampleFeedController {

  case class FeedFilmsPayload
  (
    @ApiParam(value = "The films on sale in the market", required = true) data: FeedFilmsData
  )
  case class FeedFilmsData
  (
    @ApiParam(value = "Basic market information", required = true) market: FeedMarketSerialization,
    @ApiParam(value = "The films on sale in the market", required = true) films: List[FeedFilmSerialization]
  )
  case class FeedMarketSerialization
  (
    @ApiParam(value = "The market ID", required = true) marketId: String,
    @ApiParam(value = "The market slug", required = true) marketSlug: String,
    @ApiParam(value = "The market name", required = true) marketName: String
  )
  object MarketSerialization {
    def fromFeedMarket(feedMarket: FeedMarket): FeedMarketSerialization = FeedMarketSerialization(marketId = feedMarket.id, marketSlug = feedMarket.slug, marketName = feedMarket.name)
  }
  case class FeedFilmSerialization
  (
    @ApiParam(value = "The head-office ID (HO code) of the film", required = true) id: String,
    @ApiParam(value = "The film slug", required = true) slug: String,
    @ApiParam(value = "The title of the film in Vista", required = true) vistaTitle: Option[String]
  )
  object FeedFilmSerialization {
    def fromFeedFilm(feedFilm: FeedFilm): FeedFilmSerialization = FeedFilmSerialization(id = feedFilm.id, slug = feedFilm.slug, vistaTitle = feedFilm.vistaTitle)
  }

}
