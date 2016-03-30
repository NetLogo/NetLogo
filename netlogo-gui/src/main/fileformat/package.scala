// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.api.Version
import org.nlogo.core.model.WidgetReader

package object fileformat {
  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReaders.additionalReaders

}
