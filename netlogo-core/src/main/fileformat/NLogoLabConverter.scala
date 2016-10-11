// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model
import org.nlogo.api.{ AutoConvertable, AutoConverter, LabProtocol }

import scala.util.{ Failure, Success, Try }

object NLogoLabConverter extends AutoConvertable {
  def componentName = "org.nlogo.modelsection.behaviorspace"

  def componentDescription: String = "BehaviorSpace"

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

  override def autoConvert(model: Model, converter: AutoConverter): Either[(Model, Seq[Exception]), Model] = {
    Try {
      if (model.hasValueForOptionalSection(componentName))
        model.optionalSectionValue[Seq[LabProtocol]](componentName).map(protocols =>
            model.withOptionalSection(componentName, Some(protocols.map(autoConvertProtocol(converter))), Seq())
          ).getOrElse(model)
      else
        model
    } match {
      case Failure(e: Exception) => Left((model, Seq(e)))
      case Success(m) => Right(m)
      case Failure(t) => throw t
    }
  }
}
