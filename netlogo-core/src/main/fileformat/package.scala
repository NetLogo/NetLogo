// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import java.nio.file.Path

import org.nlogo.api.{ AutoConvertable, ConfigurableModelLoader, Version }
import org.nlogo.core.{ CompilationEnvironment, Dialect, ExtensionManager, Model, LiteralParser }
import org.nlogo.core.model.{ WidgetReader, HubNetWidgetReader }
import org.nlogo.xmllib.ScalaXmlElementFactory

package object fileformat {
  type ModelConversion = (Model, Path) => ConversionResult

  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReader.defaultReaders

  def defaultAutoConvertables: Seq[AutoConvertable] = Seq(WidgetConverter, NLogoLabConverter)

  def defaultConverter: ModelConversion = (m: Model, path: Path) => SuccessfulConversion(m, m)

  def converter(
    extensionManager:       ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    literalParser:          LiteralParser,
    conversionSections:     Seq[AutoConvertable])(
      dialect:                Dialect): ModelConversion = {
        ModelConverter(extensionManager, compilationEnvironment,
          literalParser, dialect, conversionSections)
      }

  def modelSuffix(modelString: String): Option[String] =
    VersionDetector.findSuffix(modelString)

  def modelVersionAtPath(path: String): Option[Version] =
    VersionDetector.fromPath(path, basicLoader)

  def modelVersionFromString(modelString: String): Option[Version] =
    VersionDetector.fromModelContents(modelString, basicLoader)

  // basicLoader only loads the core of the model, and does no autoconversion, but has no external dependencies
  def basicLoader: ConfigurableModelLoader =
    new ConfigurableModelLoader()
      .addFormat[NLogoXFormat.Section, NLogoXFormat](new NLogoXFormat(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXModelInfo(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXLabFormat(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXModelSettings(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXPreviewCommandsFormat(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXHubNetFormat(ScalaXmlElementFactory))
      .addFormat[Array[String], NLogoFormat](new NLogoFormat)
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)

  def standardLoader(literalParser: LiteralParser) = {
    new ConfigurableModelLoader()
      .addFormat[NLogoXFormat.Section, NLogoXFormat](new NLogoXFormat(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXModelInfo(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXLabFormat(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXModelSettings(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXPreviewCommandsFormat(ScalaXmlElementFactory))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](new NLogoXHubNetFormat(ScalaXmlElementFactory))
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
