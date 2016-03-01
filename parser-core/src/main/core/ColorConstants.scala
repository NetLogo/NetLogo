// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object ColorConstants {

  // these define NetLogo's color names and how they map into the [0.0,140.0) range
  val ColorNames = Array(
    // the 13 base hues
    "gray", "red", "orange", "brown",
    "yellow", "green", "lime", "turquoise", "cyan", "sky",
    "blue", "violet", "magenta", "pink",
    // plus two special cases
    "black", "white")

  val Black = 0
  val White = 9.9

  private val ColorNumbers = Array[Double](
    // the 13 base hues
    5.0, 15.0, 25.0, 35.0, 45.0, 55.0, 65.0,
    75.0, 85.0, 95.0, 105.0, 115.0, 125.0, 135.0,
    // plus two special cases
    Black, White)

  // these method names have almost no rhyme or reason to them, so beware... - ST 4/30/05

  // input: [0-15]
  // output: [0.0-140.0)
  def getColorNumberByIndex(index: Int): Double =
    ColorNumbers(index)

  def getColorNamesArray =
    ColorNames

  // input: [0-15]
  def getColorNameByIndex(index: Int): String =
    ColorNames(index)
}
