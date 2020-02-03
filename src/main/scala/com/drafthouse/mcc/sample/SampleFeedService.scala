package com.drafthouse.mcc.sample

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.drafthouse.mcc.domain._
import com.drafthouse.mcc.sample.SampleFeedService.{FeedFilm, FeedMarket}
import com.twitter.finatra.httpclient.{HttpClient, RequestBuilder}
import com.twitter.inject.Logging
import com.twitter.util.Future
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import com.drafthouse.mcc.util.AdcHttpClient._
import io.circe.generic.auto._
import io.circe.parser._

/** An stripped-down example of how to talk to a remote REST-based service */
@Singleton
class SampleFeedService @Inject()
(
  // There will be multiple HttpClients known to the dependency injector. We differentiate them using a per-source annotation configured with the HttpClient in the HttpClientModule
  @FeedsClient httpClient: HttpClient,
  config: Config // Generally we include a reference to the config as the last parameter to services and controllers so that it's available for feature flags or other configuration points
) extends Logging {

  // An example of some configuration that might vary by environment and should therefore come from the config system
  private val feedsBasePath = config.getString("adc.remote.feeds.feedsBasePath")

  /** Answers the films from the market feed endpoint */
  def fetchFeedMarketAndFilms(marketIdOrSlug: String): Future[(FeedMarket, List[FeedFilm])] = {
    val url = s"${feedsBasePath}/marketFilms/${URLEncoder.encode(marketIdOrSlug, StandardCharsets.UTF_8.name)}"
    val req = RequestBuilder.get(url)
    for {
      resp <- httpClient.executeExpect200(req) // The AdcHttpClient handles the common expectation of a non-200 HTTP status as an error which we can just propagate for these cases
      eRawFeed = decode[RawFeed](resp.getContentString) // parse the payload
      rawMarket <- eRawFeed match {
        case Left(err) => Future.exception(AdcCirceParseErrorWithRequest(err, req, resp)) // If it does parse, bubble up the error
        case Right(RawFeed(Some(RawFeedData(Some(market))), _)) => Future.value(market)
        // The market feed erroneously reports a 200 with an "error" in the payload for bad requests and markets that don't exist in some/many cases so handle that
        case Right(RawFeed(_, Some(errorMessage))) => Future.exception(AdcMiscError(AdcError.HttpNotFound, s"No market feed found for market '${marketIdOrSlug}': ${errorMessage}"))
        // Any other problem we'll treat as a missing market (404)
        case _ => Future.exception(AdcMiscError(AdcError.HttpNotFound, s"No market feed found for market '${marketIdOrSlug}'"))
      }
      // Format the data into a clean public representation
      market = rawMarket.toFeedMarket
      films = rawMarket.films.map(_.toFeedFilm)
    } yield (market, films)
  }

  // Case classes used to mimic the structure of the source endpoint to simplify the serialization/parsing with Circe auto parsing

  private case class RawFeed
  (
    data: Option[RawFeedData],
    error: Option[String]
  )
  private case class RawFeedData
  (
    market: Option[RawMarket]
  )
  private case class RawMarket
  (
    id: String,
    slug: String,
    name: String,
    films: List[RawFilm]
  ) {
    def toFeedMarket: FeedMarket = FeedMarket(id = this.id, slug = this.slug, name = this.name)
  }
  private case class RawFilm
  (
    id: String,
    slug: String,
    name: Option[String]
  ) {
    def toFeedFilm: FeedFilm = FeedFilm(id = this.id, slug = this.slug, vistaTitle = this.name)
  }

}

object SampleFeedService {

  // Data types provided by the service

  case class FeedMarket
  (
    id: String,
    slug: String,
    name: String
  )

  case class FeedFilm
  (
    id: String,
    slug: String,
    vistaTitle: Option[String]
  )

}