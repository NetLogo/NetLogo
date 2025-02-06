// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.Path

import org.nlogo.api.{ AutoConvertable, ConfigurableModelLoader }
import org.nlogo.core.{ CompilationEnvironment, Dialect, ExtensionManager, LibraryManager, LiteralParser, Model }
import org.nlogo.core.model.WidgetReader

object FileFormat {

  type ModelConversion = (Model, Path) => ConversionResult

  def nlogoReaders(is3D: Boolean): Map[String, WidgetReader] =
    if (is3D)
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
    else
      Map()

  def hubNetReaders: Map[String, WidgetReader] =
    HubNetWidgetReaders.additionalReaders

  def defaultAutoConvertables: Seq[AutoConvertable] = Seq(WidgetConverter, NLogoLabConverter)

  def defaultConverter: ModelConversion = (m: Model, path: Path) => SuccessfulConversion(m, m)

  def converter(
    extensionManager:       ExtensionManager,
    libManager:             LibraryManager,
    compilationEnvironment: CompilationEnvironment,
    literalParser:          LiteralParser,
    conversionSections:     Seq[AutoConvertable])(dialect: Dialect): ModelConversion = {
    new ChainConverter(
      Seq(
        ModelConverter(extensionManager, libManager, compilationEnvironment, literalParser, dialect, conversionSections),
        new PlotConverter(extensionManager, libManager, compilationEnvironment, literalParser, dialect, conversionSections)
      )
    )
  }

  // basicLoader only loads the core of the model, and does no autoconversion, but has no external dependencies
  def basicLoader: ConfigurableModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat)
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)

  def standardLoader(literalParser: LiteralParser, editNames: Boolean = false): ConfigurableModelLoader = {
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat)
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(literalParser))
      .addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(literalParser, editNames))
      .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat)
      .addSerializer[Array[String], NLogoThreeDFormat](new NLogoThreeDLabFormat(literalParser, editNames))
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDModelSettings)
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDPreviewCommandsFormat)
  }

  def standardXMLLoader(headless: Boolean, literalParser: LiteralParser, editNames: Boolean = false): NLogoXMLLoader =
    new NLogoXMLLoader(headless, literalParser, editNames)

  def standardAnyLoader(headless: Boolean, literalParser: LiteralParser, editNames: Boolean = false): NLogoAnyLoader =
    new NLogoAnyLoader(List(standardXMLLoader(headless, literalParser, editNames), standardLoader(literalParser, editNames)))

}
