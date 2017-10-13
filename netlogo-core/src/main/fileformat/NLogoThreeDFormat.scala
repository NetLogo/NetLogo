// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.{ ModelFormat, ThreeDVersion, WorldDimensions3D }
import org.nlogo.core.{ View, UpdateMode }
import org.nlogo.core.model.WidgetReader

class NLogoThreeDFormat
  extends ModelFormat[Array[String], NLogoThreeDFormat]
  with AbstractNLogoFormat[NLogoThreeDFormat] {
    def versionObject = ThreeDVersion
    val is3DFormat = true
    def name: String = "nlogo3d"
    override def widgetReaders =
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)

  override lazy val defaultView: View = View(left = 210, top = 10, right = 649, bottom = 470,
    dimensions = new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0), fontSize = 10, updateMode = UpdateMode.Continuous,
    showTickCounter = true, frameRate = 30)
  }
