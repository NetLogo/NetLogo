// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.net.URI

import org.nlogo.api.{ AutoConvertable, ModelFormat, WorldDimensions3D }
import org.nlogo.core.{ Model, View, UpdateMode }
import org.nlogo.core.model.WidgetReader

class NLogoThreeDFormat(modelConverter: (Model, Seq[AutoConvertable]) => Model)
  extends ModelFormat[Array[String], NLogoThreeDFormat]
  with AbstractNLogoFormat[NLogoThreeDFormat] {
    val is3DFormat = true
    def name: String = "nlogo3d"
    override def widgetReaders =
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)

  override lazy val defaultView: View = View(left = 210, top = 10, right = 649, bottom = 470,
    dimensions = new WorldDimensions3D(-16, 16, -16, 16, -16, 16, 13.0), fontSize = 10, updateMode = UpdateMode.Continuous,
    showTickCounter = true, frameRate = 30)
  }
