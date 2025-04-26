// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object Implicits {
  implicit class RichString(val s: String) {
    def stripTrailingWhiteSpace = s.linesIterator
      .map(_.replaceAll("\\s+$", "")) // strips whitespace at the end of each line
      .toSeq.reverse.dropWhile(_.isEmpty).reverse // drop empty lines at the end
      .mkString("\n") // and make the string (without a new line at the end)
  }
}
