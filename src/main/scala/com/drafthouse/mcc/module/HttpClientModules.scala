package com.drafthouse.mcc.module

import com.drafthouse.mcc.domain.{DrafthouseClient, FeedsClient}
import javax.inject.Singleton
import com.google.inject.Provides
import com.twitter.finagle.{Filter, Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.httpclient.HttpClient
import com.twitter.finatra.httpclient.modules.HttpClientModule
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.TwitterModule
import com.twitter.util.{Duration, StorageUnit}
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import com.twitter.conversions.time._

object HttpClientModules {

  val modules: Seq[TwitterModule] = Seq(
    FeedsHttpClientModule.apply(),
    DrafthouseClientModule.apply()
  )

  private abstract class BasicHttpClientModule() extends TwitterModule {

    protected def provideHttpClient
    (
      mapper: FinatraObjectMapper,
      host: String, port: Int = 443, tls: Boolean,
      requestTimeout: Duration = 30.seconds,
      headers: Map[String, String] = Map(),
      filter: Option[Filter[Request, Response, Request, Response]] = None
    ): HttpClient = {

      val httpClientModule = new HttpClientModule {

        override def sslHostname: Option[String] = if (tls) Some(host) else None
        override def dest: String = s"$host:$port"
        override def defaultHeaders: Map[String, String] = Map("Host" -> host) ++ headers

        @Singleton @Provides
        override def provideHttpService: Service[Request, Response] = {
          val client = sslHostname match {
            case Some(sslHostname) => Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual.withTls(sslHostname)
            case _ => Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual
          }
          val filteredClient = filter.map(f => client.filtered(f)).getOrElse(client)
          filteredClient
            .withSessionQualifier.noFailFast
            .withRequestTimeout(requestTimeout)
            .withMaxRequestSize(StorageUnit.fromMegabytes(25))
            .withMaxResponseSize(StorageUnit.fromMegabytes(25))
            .newService(dest)
        }
      }

      httpClientModule.provideHttpClient(mapper, httpClientModule.provideHttpService)
    }

  }

  private object FeedsHttpClientModule {
    def apply(): BasicHttpClientModule = new BasicHttpClientModule {
      @FeedsClient @Provides @Singleton
      def provideHttpClient(mapper: FinatraObjectMapper, config: Config): HttpClient =
        super.provideHttpClient(
          mapper = mapper,
          host = config.as[String]("adc.remote.feeds.host"),
          port = config.as[Int]("adc.remote.feeds.port"),
          tls = config.as[Boolean]("adc.remote.feeds.tls")
        )
    }
  }

  private object DrafthouseClientModule {
    def apply(): BasicHttpClientModule = new BasicHttpClientModule {
      @DrafthouseClient @Provides @Singleton
      def provideHttpClient(mapper: FinatraObjectMapper, config: Config): HttpClient =
        super.provideHttpClient(
          mapper = mapper,
          host = config.as[String]("adc.remote.drafthouse.host"),
          port = config.as[Int]("adc.remote.drafthouse.port"),
          tls = config.as[Boolean]("adc.remote.drafthouse.tls")
        )
    }
  }

}

