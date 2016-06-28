// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.api.{ ConfigurableModelLoader, ModelLoader, NetLogoLegacyDialect, NetLogoThreeDDialect, Version }
import org.nlogo.core.{ CompilationEnvironment, Dialect, ExtensionManager, Model, LiteralParser }
import org.nlogo.core.model.WidgetReader

package object fileformat {
  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReaders.additionalReaders

  // make it so basicLoader doesn't need extensionManager, compilationEnvironment
  def basicLoader: ModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat((m, _) => m))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)

  def standardLoader(literalParser: LiteralParser, extensionManager: ExtensionManager, compilationEnvironment: CompilationEnvironment): ConfigurableModelLoader = {
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat(converter(extensionManager, compilationEnvironment, NetLogoLegacyDialect)))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(literalParser))
      .addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(literalParser))
      .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat(converter(extensionManager, compilationEnvironment, NetLogoThreeDDialect)))
      .addSerializer[Array[String], NLogoThreeDFormat](new NLogoThreeDLabFormat(literalParser))
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDModelSettings)
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDPreviewCommandsFormat)
  }


  def converter(extensionManager: ExtensionManager, compilationEnvironment: CompilationEnvironment, dialect: Dialect): ModelConverter = {
    val modelConversions = ((m: Model) => AutoConversionList.conversions.collect {
      case (version, conversionSet) if Version.numericValue(m.version) < Version.numericValue(version) => conversionSet
    })
    new ModelConverter(extensionManager, compilationEnvironment, dialect, modelConversions)
  }
}
