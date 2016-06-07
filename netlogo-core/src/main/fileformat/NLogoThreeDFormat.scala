// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.net.URI

import org.nlogo.api.ModelFormat
import org.nlogo.core.model.WidgetReader

class NLogoThreeDFormat(val autoConvert: String => String => String)
  extends ModelFormat[Array[String], NLogoThreeDFormat]
  with AbstractNLogoFormat[NLogoThreeDFormat] {
    val is3DFormat = true
    def name: String = "nlogo3d"
    override def widgetReaders =
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
  }
