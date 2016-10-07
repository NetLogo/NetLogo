// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.api.{ AutoConvertable, ConfigurableModelLoader, ModelLoader, Version }
import org.nlogo.core.{ CompilationEnvironment, Dialect, ExtensionManager, Model, LiteralParser }
import org.nlogo.core.model.WidgetReader

import scala.util.Try

package object fileformat {
  type ModelConversion = Model => ConversionResult

  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReaders.additionalReaders

  def defaultAutoConvertables: Seq[AutoConvertable] = Seq(WidgetConverter, NLogoLabConverter)

  def defaultConverter: ModelConversion = (m: Model) => NoConversionNeeded(m)

  def converter(
    extensionManager:       ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    literalParser:          LiteralParser,
    conversionSections:     Seq[AutoConvertable])(
      dialect:                Dialect): ModelConversion = {
        ModelConverter(extensionManager, compilationEnvironment,
          literalParser, dialect, conversionSections)
      }

  // basicLoader only loads the core of the model, and does no autoconversion, but has no external dependencies
  def basicLoader: ConfigurableModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat)
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)

  def standardLoader(literalParser: LiteralParser) = {
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat)
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(literalParser))
      .addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(literalParser))
      .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat)
      .addSerializer[Array[String], NLogoThreeDFormat](new NLogoThreeDLabFormat(literalParser))
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDModelSettings)
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDPreviewCommandsFormat)
  }
}
