package com.drafthouse.mcc.domain

import com.drafthouse.mcc.domain.AdcError.ErrorCode
import com.twitter.finagle.http.{Request, Response, Status}

object AdcError {
  @inline final val CategoryCodeResource = 100
  @inline final val CategoryCodeHttpStatus = 101
  @inline final val CategoryCodeMisc = 105

  @inline final val HttpNotFound = ErrorCode(CategoryCodeHttpStatus, Status.NotFound.code, "Resource Unavailable")
  @inline final val BadRequest = ErrorCode(CategoryCodeHttpStatus, Status.BadRequest.code, "Bad Request")
  @inline final val HttpUnauthorized = ErrorCode(CategoryCodeHttpStatus, Status.Unauthorized.code, "Unauthorized")
  @inline final val HttpForbidden = ErrorCode(CategoryCodeHttpStatus, Status.Forbidden.code, "Forbidden")
  @inline final val CirceParseError = ErrorCode(CategoryCodeResource, 600, "Parse Error")
  @inline final val UnknownError = ErrorCode(CategoryCodeMisc, 500, "Unknown Error")

  /**
    * Information about the specific error.
    * @param category general group describing the source of the error.
    * @param code unique identifier of a specific error.
    * @param description text to display when describing the error.
    */
  case class ErrorCode(category: Int, code: Long, description: String) {
    /** Returns a string representation of the error code. */
    override def toString: String = s"${category}-${code}"
    /** Returns a string representation of the rror code which includes the description text. */
    def toErrorDocumentation: String = s"${category}-${code}: ${description}"
  }
}

/**
  * Base class for detailed error information extracted from an exception.
  * @param errorCode detailed error category, code and description.
  * @param message text that is displayed when describing the error.
  * @param cause the exception which caused the error.
  */
abstract class BaseAdcError(errorCode: AdcError.ErrorCode, message: String, cause: Throwable)
  extends Exception(message, cause) {

  /**
    * Constructor when we only have the error code.
    * @param errorCode detailed error category, code and description.
    * @return constructed object.
    */
  def this(errorCode: AdcError.ErrorCode) = this(errorCode, null, null)

  /**
    * Constructor when we only have the error code and description.
    * @param errorCode detailed error category, code and description.
    * @param description text that is displayed when describing the error.
    * @return constructed object.
    */
  def this(errorCode: AdcError.ErrorCode, description: String) = this(errorCode, description, null)

  /**
    * Constructor when we only have the error code and cause.
    * @param errorCode detailed error category, code and description.
    * @param cause the exception which caused the error.
    * @return constructed object.
    */
  def this(errorCode: AdcError.ErrorCode, cause: Throwable) = this(errorCode, null, cause)

  /** Returns the detail message string of this throwable. */
  override def getMessage: String =
    if (message != null) message else s"${errorCode.category}-${errorCode.code} (${errorCode.description})"

  /** Returns the error category, code and description of this error. */
  def getErrorCode: AdcError.ErrorCode = errorCode
}

/**
  *
  * @param errorCode detailed error category, code and description.
  * @param message text that is displayed when describing the error.
  * @param cause the exception which caused the error.
  * @param request the optional http request which caused the error.
  * @param response the optional http response to the reported error.
  */
abstract class AdcErrorWithRequestResponse(errorCode: ErrorCode, message: String, cause: Throwable, request: Option[Request], response: Option[Response]) extends BaseAdcError(errorCode, message, cause) {
  def getRequest: Option[Request] = request
  def getResponse: Option[Response] = response
}

/**
  *
  * @param errorCode detailed error category, code and description.
  * @param message text that is displayed when describing the error.
  * @param cause the exception which caused the error.
  */
case class AdcMiscError(errorCode: AdcError.ErrorCode, message: String, cause: Throwable = null) extends BaseAdcError(errorCode, message, cause)

/**
  *
  * @param errorCode detailed error category, code and description.
  * @param message text that is displayed when describing the error.
  * @param request http request which is associated with the error.
  * @param response http response which results from the error.
  * @param cause the exception which caused the error. may be null.
  */
case class AdcMiscErrorReqResp(errorCode: AdcError.ErrorCode, message: String, request: Request, response: Response, cause: Throwable = null) extends AdcErrorWithRequestResponse(errorCode, message, cause, Option(request), Option(response))

/**
  *
  * @param httpStatus http status value associated with the response.
  * @param message text that is displayed when describing the error.
  * @param request the http request which caused the error.
  * @param response the http response to the error.
  */
case class AdcHttpError(httpStatus: Status, message: String = null, request: Request, response: Response)
  extends AdcErrorWithRequestResponse(AdcError.ErrorCode(AdcError.CategoryCodeHttpStatus, httpStatus.code, httpStatus.reason),
    if (message != null) message else httpStatus.reason, null, Option(request), Option(response))

/**
  *
  * @param error the error reported by circe.
  */
case class AdcCirceParseError(error: io.circe.Error) extends BaseAdcError(AdcError.CirceParseError, error.getLocalizedMessage, error)

/**
  *
  * @param error the error reported by circe.
  * @param request the http request which caused the error.
  * @param response the http response which reported the error.
  */
case class AdcCirceParseErrorWithRequest(error: io.circe.Error, request: Request, response: Response) extends
  AdcErrorWithRequestResponse(AdcError.CirceParseError, error.getLocalizedMessage, error, Option(request), Option(response))

/**
  *
  * @param messages zero or more string error messages associated with a bad request.
  */
case class AdcBadRequestError(messages: List[String]) extends BaseAdcError(AdcError.BadRequest)
