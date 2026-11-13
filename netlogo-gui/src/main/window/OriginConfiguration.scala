// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext

abstract class OriginType(name: String) {
  override def toString: String =
    name
}

object OriginType {
  case object Center extends OriginType(loc"Center")
  case object Corner extends OriginType(loc"Corner")
  case object Edge extends OriginType(loc"Edge")
  case object Custom extends OriginType(loc"Custom")
}

case class OriginConfiguration(name: String, x: Double, y: Double, z: Double = 0.5) {
  override def toString: String =
    name
}
