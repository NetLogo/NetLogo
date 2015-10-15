// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ ColorConstants, Nobody }

object Constants {
  val colorConstants: Map[String, AnyRef] =
    (for ((name, index) <- ColorConstants.getColorNamesArray.zipWithIndex)
     yield (name.toUpperCase -> Double.box(ColorConstants.getColorNumberByIndex(index)))).toMap +
    ("GREY" ->
      Double.box(ColorConstants.getColorNumberByIndex(ColorConstants.getColorNamesArray.indexOf("gray"))))
  val otherConstants = Map[String, AnyRef](
    "FALSE" -> Boolean.box(false),
    "TRUE" -> Boolean.box(true),
    "NOBODY" -> Nobody,
    "E" -> Double.box(StrictMath.E),
    "PI" -> Double.box(StrictMath.PI))
  val constants: Map[String, AnyRef] =
    otherConstants ++ colorConstants
  def get(s: String): Option[AnyRef] =
    constants.get(s.toUpperCase)
}
