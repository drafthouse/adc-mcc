package com.drafthouse.mcc.core

import com.drafthouse.mcc.domain.{AdcBadRequestError, AdcError, AdcErrorWithRequestResponse, BaseAdcError}
import com.fasterxml.jackson.databind.ObjectMapper
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.inject.Logging
import com.typesafe.config.Config
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}

@Singleton
class AdcExceptionMapper @Inject()
(
  response: ResponseBuilder,
  objectMapper: ObjectMapper,
  config: Config
) extends ExceptionMapper[Exception] with Logging {

  /** Maps exceptions into error payloads and HTTP status codes */
  override def toResponse(request: Request, ex: Exception): Response = {
    val (json, errorCode) = ex match {
      case ex: AdcBadRequestError => (AdcExceptionMapper.payloadForBadRequest(ex).asJson, AdcError.BadRequest)
      case ex: AdcErrorWithRequestResponse => (AdcExceptionMapper.payloadForErrorWithRequest(ex).asJson, ex.getErrorCode)
      case ex: BaseAdcError => (AdcExceptionMapper.payloadForError(ex).asJson, ex.getErrorCode)
      case ex => (AdcExceptionMapper.payloadForException(ex).asJson, AdcError.UnknownError)
    }
    (errorCode match {
      case AdcError.BadRequest => response.badRequest
      case AdcError.HttpUnauthorized => response.unauthorized
      case AdcError.HttpForbidden => response.forbidden
      case AdcError.HttpNotFound => response.notFound
      case _ => response.internalServerError
    }).json(json.noSpaces)
  }

}

object AdcExceptionMapper {

  private def payloadForException(ex: Exception): ErrorPayload = ErrorPayload(
    ErrorSerialization(AdcError.UnknownError, Option(ex.getMessage), ex.getClass.getName, formatStackTrace(ex))
  )

  private def payloadForError(ex: BaseAdcError): ErrorPayload = ErrorPayload(
    ErrorSerialization(ex.getErrorCode, Option(ex.getMessage), ex.getClass.getName, formatStackTrace(ex))
  )

  private def payloadForErrorWithRequest(ex: AdcErrorWithRequestResponse): ErrorWithRequestPayload = ErrorWithRequestPayload(
    ErrorWithRequestSerialization(ex.getErrorCode, Option(ex.getMessage), ex.getClass.getName, formatStackTrace(ex),
      ex.getRequest.map(req => RequestSerialization(req.method.name, req.uri, req.headerMap.toMap, req.contentString)),
      ex.getResponse.map(resp => ResponseSerialization(StatusSerialization(resp.status.code, resp.status.reason), resp.headerMap.toMap, resp.contentString))
    )
  )

  private def payloadForBadRequest(ex: AdcBadRequestError): BadRequestPayload = BadRequestPayload(
    BadRequestSerialization(AdcError.BadRequest, ex.messages)
  )

  private def formatStackTrace(t: Throwable): List[String] = {
    t.getStackTrace.map(_.toString).toList.take(7) :+ "...more..."
  }

  case class ErrorSerialization
  (
    errorCode: AdcError.ErrorCode,
    description: Option[String],
    errorType: String,
    stackTrace: List[String]
  )
  case class ErrorPayload
  (
    error: ErrorSerialization
  )

  case class ErrorWithRequestSerialization
  (
    errorCode: AdcError.ErrorCode,
    description: Option[String],
    errorType: String,
    stackTrace: List[String],
    request: Option[RequestSerialization],
    response: Option[ResponseSerialization]
  )
  case class ErrorWithRequestPayload
  (
    error: ErrorWithRequestSerialization
  )

  case class RequestSerialization
  (
    method: String,
    uri: String,
    headers: Map[String, String],
    content: String
  )
  case class ResponseSerialization
  (
    status: StatusSerialization,
    headers: Map[String, String],
    content: String
  )
  case class StatusSerialization
  (
    statusCode: Int,
    reason: String
  )

  case class BadRequestSerialization
  (
    errorCode: AdcError.ErrorCode,
    messages: List[String]
  )
  case class BadRequestPayload
  (
    error: BadRequestSerialization
  )

}
