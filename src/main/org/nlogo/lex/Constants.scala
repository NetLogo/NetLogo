// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api.{ Color, Nobody }

object Constants {
  val colorConstants =
    (for ((name, index) <- Color.getColorNamesArray.zipWithIndex)
     yield (name.toUpperCase -> Color.getColorNumberByIndex(index))).toMap +
    ("GREY" -> Color.getColorNumberByIndex(Color.getColorNamesArray.indexOf("gray")))
  val otherConstants = Map(
    "FALSE" -> false,
    "TRUE" -> true,
    "NOBODY" -> Nobody,
    "E" -> StrictMath.E,
    "PI" -> StrictMath.PI)
  val constants = otherConstants ++ colorConstants
  def isConstant(s: String) =
    constants.contains(s.toUpperCase)
  // caller's responsibility to validate s first
  def get(s: String) =
    constants.get(s.toUpperCase).get
}
