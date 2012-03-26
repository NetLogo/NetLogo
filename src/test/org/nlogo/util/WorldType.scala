// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

class WorldType(val xWrap: Boolean, val yWrap: Boolean)

object WorldType {
  case object Box extends WorldType(false, false)
  case object VerticalCyl extends WorldType(true, false)
  case object HorizontalCyl extends WorldType(false, true)
  case object Torus extends WorldType(true, true)
  def all = List(Box, VerticalCyl, HorizontalCyl, Torus)
}
