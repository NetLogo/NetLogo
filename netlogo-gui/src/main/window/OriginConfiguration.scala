// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.I18N

abstract class OriginType(name: String) {
  override def toString: String =
    name
}

object OriginType {
  case object Center extends OriginType(I18N.gui.get("edit.viewSettings.origin.location.center"))
  case object Corner extends OriginType(I18N.gui.get("edit.viewSettings.origin.location.corner"))
  case object Edge extends OriginType(I18N.gui.get("edit.viewSettings.origin.location.edge"))
  case object Custom extends OriginType(I18N.gui.get("edit.viewSettings.origin.location.custom"))
}

sealed trait Extent

object Extent {
  case object Min extends Extent
  case object Max extends Extent
  case object Center extends Extent
  case object Custom extends Extent
}

case class OriginConfiguration(name: String, x: Extent, y: Extent, z: Extent = Extent.Center) {
  override def toString: String =
    name
}
