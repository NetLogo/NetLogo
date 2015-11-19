package org.nlogo.util

import scala.collection.immutable.StringLike

object Implicits {

  implicit class RichString(val s: String) {
    def stripTrailingWhiteSpace = RichStringLike(s).stripTrailingWhiteSpace
  }

  implicit class RichStringLike[Repr](val s: StringLike[Repr]) {
    def stripTrailingWhiteSpace = s.lines
      .map(_.replaceAll("\\s+$", "")) // strips whitespace at the end of each line
      .toSeq.reverse.dropWhile(_.isEmpty).reverse // drop empty lines at the end
      .mkString("\n") // and make the string (without a new line at the end)
  }

}
