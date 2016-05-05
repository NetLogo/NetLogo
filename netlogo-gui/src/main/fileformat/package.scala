// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.api.{ ConfigurableModelLoader, ModelLoader, Version }
import org.nlogo.core.LiteralParser
import org.nlogo.core.model.WidgetReader

package object fileformat {
  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReaders.additionalReaders

  def standardLoader(literalParser: LiteralParser, autoConvert: String => String => String): ModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat(autoConvert))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(literalParser, autoConvert))
      .addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(autoConvert, literalParser))
}
