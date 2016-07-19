// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo

import org.nlogo.api.{ AutoConvertable, ConfigurableModelLoader, ModelLoader, Version }
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

  def defaultAutoConversion: (Model, Seq[AutoConvertable]) => Model =
    (m, _) => m

  // basicLoader only loads the core of the model, and does no autoconversion, but has no external dependencies
  def basicLoader: ConfigurableModelLoader =
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat(defaultAutoConversion))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)

  def standardLoader(literalParser: LiteralParser,
    nlogoConversion: (Model, Seq[AutoConvertable]) => Model = defaultAutoConversion,
    nlogoThreeDConversion: (Model, Seq[AutoConvertable]) => Model = defaultAutoConversion) = {
    new ConfigurableModelLoader()
      .addFormat[Array[String], NLogoFormat](new NLogoFormat(nlogoConversion))
      .addSerializer[Array[String], NLogoFormat](NLogoModelSettings)
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(literalParser))
      .addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(literalParser))
      .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat(nlogoThreeDConversion))
      .addSerializer[Array[String], NLogoThreeDFormat](new NLogoThreeDLabFormat(literalParser))
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDModelSettings)
      .addSerializer[Array[String], NLogoThreeDFormat](NLogoThreeDPreviewCommandsFormat)
  }
}
