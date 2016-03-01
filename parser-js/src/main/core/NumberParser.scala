// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// This is highly performance-critical code for import-world. - ST 4/7/11

import java.lang.{ Double => JDouble, Long => JLong }

object NumberParser {
  val IsTooLarge = "Number too large"
  val IsTooLargeForExactness =
    "is too large to be represented exactly as an integer in NetLogo"
  val IllegalFormat = "Illegal number format"

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
        Left(IllegalFormat)
    }
  }
}
