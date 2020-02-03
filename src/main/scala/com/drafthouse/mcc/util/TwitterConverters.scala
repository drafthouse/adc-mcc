package com.drafthouse.mcc.util

import com.twitter.{util => twitter}

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/** Implicit conversion methods for swapping between Scala and Twitter Futures and Trys */
object TwitterConverters {
  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r) => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  implicit def scalaToTwitterFuture[T](f: Future[T])(implicit ec: scala.concurrent.ExecutionContext): twitter.Future[T] = {
    val promise = twitter.Promise[T]()
    f.onComplete(promise update _)
    promise
  }

  implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    f.respond(promise complete _)
    promise.future
  }

  implicit def eitherToTwitterFuture[E <: Exception, T](e: Either[E, T]): twitter.Future[T] = {
    e match {
      case Left(e) => twitter.Future.exception(e)
      case Right(v) => twitter.Future.value(v)
    }
  }
}