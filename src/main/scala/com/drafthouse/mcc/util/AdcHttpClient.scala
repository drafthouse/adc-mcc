package com.drafthouse.mcc.util

import com.drafthouse.mcc.domain.AdcHttpError
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.httpclient.HttpClient
import com.twitter.util.Future

/** Convenience type class for handling the common HTTP error cases */
object AdcHttpClient {

  implicit class HttpClientWith200Handling(httpClient: HttpClient) {
    def executeExpect200(req: Request): Future[Response] = {
      for {
        resp <- httpClient.execute(req)
        result <- resp.statusCode match {
          case 200 => Future.value(resp)
          case _ => Future.exception(AdcHttpError(Status(resp.statusCode), req.uri, req, resp))
        }
      } yield result
    }
    def executeNoExpectations(req: Request): Future[Response] = {
      for {
        resp <- httpClient.execute(req)
      } yield resp
    }
  }

}
