// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ SourceLocatable, SourceLocation }

import scala.util.{ Failure, Success, Try }

// So why do we have this thing that looks like Try, but isn't Try?
//
// Two reasons:
// 1) Our domain doesn't match up correctly with Try. Try gives a full java exception,
// we typically have only failure cases which are normal (we expect users to give us unparseable code)
// and not location sensitive (it doesn't matter where in *our* code it comes from)
//
// 2) If we want to handle multiple errors down the line, we'll need a way of communicating about errors
// that *we* own, not scala.util

object ParseResult {
  def apply[A](a: A): ParseResult[A] = SuccessfulParse(a)
  def fromTry[A](t: Try[A]) =
    t match {
      case Success(x) => SuccessfulParse(x)
      case Failure(e: core.CompilerException) => FailedParse(ParseFailure(e.getMessage, e.start, e.end, e.filename))
      case Failure(e) => throw e
    }

  def fail(message: String, locatable: SourceLocatable): FailedParse =
    fail(message, locatable.sourceLocation)
  def fail(message: String, location: SourceLocation): FailedParse =
    fail(message, location.start, location.end, location.filename)
  def fail(message: String, start: Int, end: Int, filename: String): FailedParse =
    FailedParse(ParseFailure(message, start, end, filename))
}

sealed trait ParseResult[+A] {
  def flatMap[B](f: A => ParseResult[B]): ParseResult[B]
  def map[B](f: A => B): ParseResult[B]
  def get: A
  def getOrElse[B >: A](b: B): B
  def exists(f: A => Boolean): Boolean
  def recoverWith[B >: A](f: PartialFunction[ParseFailure, ParseResult[B]]): ParseResult[B]
}

case class ParseFailure(message: String, start: Int, end: Int, filename: String) {
  def toException = new core.CompilerException(message, start, end, filename)
}

case class FailedParse(failure: ParseFailure) extends ParseResult[Nothing] {
  def flatMap[A](f: Nothing => ParseResult[A]): ParseResult[A] = this
  def map[A](f: Nothing => A): ParseResult[A] = this
  def get = throw failure.toException
  def getOrElse[B >: Nothing](b: B): B = b
  def exists(f: Nothing => Boolean): Boolean = false
  def recoverWith[A](f: PartialFunction[ParseFailure, ParseResult[A]]): ParseResult[A] =
    if (f.isDefinedAt(failure)) f(failure)
    else this
}

case class SuccessfulParse[A](val parsed: A) extends ParseResult[A] {
  def flatMap[B](f: A => ParseResult[B]): ParseResult[B] = f(parsed)
  def map[B](f: A => B): ParseResult[B] = new SuccessfulParse(f(parsed))
  def get = parsed
  def getOrElse[B >: A](b: B): B = parsed
  def exists(f: A => Boolean): Boolean = f(parsed)
  def recoverWith[B >: A](f: PartialFunction[ParseFailure, ParseResult[B]]): ParseResult[A] =
    this
}

/*
object TypeMismatch {
  def unapply(t: TypeMismatch): Option[(Int, Int)] = Some((t.expectedType, t.actualType))
}

object TypeMismatch {
  def message(instructionName: String, expectedType: Int, actualType: Int): String =
    s"$instructionName expected this input to be ${TypeNames.aName(expectedType)}, but got ${TypeNames.aName(displayedReportedType)} instead"
}


class TypeMismatch(val arg: core.Expression, message: String, val expectedType: Int, val actualType: Int) extends
  ParseFailure(message, arg.start, arg.end, arg.filename) {

  }
  */
