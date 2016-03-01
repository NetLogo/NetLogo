// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import scala.util.matching.Regex

object TestUtils {
  // scala.js `toString` for numeric values gives different results than jvm scala.
  // We need do a little cleanup to match the outputs - 2/17/15 RG
  def cleanJsNumbers(rawJs: String): String = {
    val trailingZeroNumbers  =
      new Regex("""(\d)\.0(\D)""", "digitBefore", "nonDigitAfter")
    val scientificNotation   =
      new Regex("""(\d)+E(-?)(\d+)""", "coefficient", "sign", "exponent")
    val trailingZerosRemoved = trailingZeroNumbers.replaceAllIn(rawJs, {
      m =>
        s"${m.group("digitBefore")}${m.group("nonDigitAfter")}"
    })
    scientificNotation.replaceAllIn(trailingZerosRemoved, {
      m =>
        m.group("sign") match {
          case "-" => s"0.${"0" * (m.group("exponent").toInt - 1)}${m.group("coefficient")}"
          case ""  => s"${m.group("coefficient")}${"0" * m.group("exponent").toInt}"
        }
    })
  }
}
