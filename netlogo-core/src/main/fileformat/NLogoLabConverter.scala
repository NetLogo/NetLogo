// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model
import org.nlogo.api.{ AutoConvertable, AutoConverter, LabProtocol }

import scala.util.Try

object NLogoLabConverter extends AutoConvertable {
  def componentName = "org.nlogo.modelsection.behaviorspace"

  def needingConversion(needsConversion: String => Boolean, protocol: LabProtocol): Boolean = {
    import protocol._
    needsConversion(setupCommands) || needsConversion(goCommands) || needsConversion(finalCommands) ||
      metrics.exists(needsConversion) || needsConversion(exitCondition)
  }

  def autoConvertProtocol(converter: AutoConverter)(protocol:LabProtocol): LabProtocol = {
    import protocol._
    new LabProtocol(name,
      converter.convertStatement(setupCommands),
      converter.convertStatement(goCommands),
      converter.convertStatement(finalCommands),
      repetitions, sequentialRunOrder, runMetricsEveryStep, timeLimit,
      if (exitCondition == "") "" else converter.convertReporterExpression(exitCondition),
      metrics.map(converter.convertReporterExpression),
      valueSets)
  }

  override def requiresAutoConversion(model: Model, needsConversion: String => Boolean): Boolean =
    model.optionalSectionValue[Seq[LabProtocol]](componentName)
      .exists(protocols => protocols.exists(needingConversion(needsConversion, _)))

  override def autoConvert(model: Model, converter: AutoConverter): Try[Model] = {
    Try {
      if (model.hasValueForOptionalSection(componentName))
        model.optionalSectionValue[Seq[LabProtocol]](componentName).map(protocols =>
            model.withOptionalSection(componentName, Some(protocols.map(autoConvertProtocol(converter))), Seq())
          ).getOrElse(model)
      else
        model
    }
  }
}
