// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.api.{ ConfigurableModelLoader, ModelLoader, Version }
import org.nlogo.core.{ CompilationEnvironment, ExtensionManager, LiteralParser }
import org.nlogo.core.model.WidgetReader

package object fileformat {
  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReaders.additionalReaders

  def basicLoader(extensionManager: ExtensionManager, compilationEnvironment: CompilationEnvironment): ModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat(AutoConversionList.conversions, extensionManager, compilationEnvironment))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)

  def standardLoader(literalParser: LiteralParser, extensionManager: ExtensionManager, compilationEnvironment: CompilationEnvironment): ConfigurableModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat(AutoConversionList.conversions, extensionManager, compilationEnvironment))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(literalParser))
      .addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(literalParser))
      .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat)
      .addSerializer[Array[String], NLogoThreeDFormat](new NLogoThreeDLabFormat(literalParser))
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDModelSettings)
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDPreviewCommandsFormat)
}
