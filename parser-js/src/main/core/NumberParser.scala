// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.util.matching.Regex

// This is highly performance-critical code for import-world. - ST 4/7/11

import java.lang.{ Double => JDouble, Long => JLong }

object NumberParser {
  val IsTooLarge = "Number too large"
  val IsTooLargeForExactness =
    "is too large to be represented exactly as an integer in NetLogo"
  val IllegalFormat = "Illegal number format"

  // this regex is *only* used for determining whether we should display
  // an "Illegal number format" or "Too large" error. If it's used for
  // anything else it should be suitably tested and hardened.
  // Also note that it's different than in java because we're in scala-js. RG 4/29/16
  val numberFormatRegex = new Regex("^-?[0-9]*(\\.[0-9]*)?([Ee]-?[0-9]+)?$")

  def parse(text: String): Either[String, JDouble] = {
    try {
      // parseDouble is costly, especially if it fails, so bail first if we can
      val hasDigit = {
        val size = text.size
        size > 0 && {
          var i = 0
          if(text(i) == '-')
            i += 1
          if(i < size && text(i) == '.')
            i += 1
          i < size && text(i).isDigit
        }
      }
      if(!hasDigit)
        Left("Illegal number format")
      else {
        def outOfRange = {
          val l = JLong.parseLong(text)
          l > 9007199254740992L || l < -9007199254740992L
        }
        // reject integer constants out of range representable exactly in a double - ST 1/29/08
        val d = JDouble.parseDouble(text)
        if (d.isInfinite)
          Left(IsTooLarge)
        else if (".eE".exists(text.contains(_)))
          Right(d)
        else if (outOfRange)
          Left(s"$text $IsTooLargeForExactness")
        else
          Right(d)
      }
    }
    catch {
      case ex: NumberFormatException =>
        if (numberFormatRegex.findFirstIn(text).isDefined)
          Left(s"$text $IsTooLargeForExactness")
        else
          Left(IllegalFormat)
    }
  }
}
