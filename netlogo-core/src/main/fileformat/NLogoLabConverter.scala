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
    needsConversion(preExperimentCommands) || needsConversion(setupCommands) || needsConversion(goCommands) ||
      needsConversion(postRunCommands) || needsConversion(postExperimentCommands) ||
      metrics.exists(needsConversion) || needsConversion(exitCondition)
  }

  def autoConvertProtocol(converter: AutoConverter)(protocol: LabProtocol): LabProtocol = {
    import protocol._
    protocol.copy(
      name,
      converter.convertStatement(preExperimentCommands),
      converter.convertStatement(setupCommands),
      converter.convertStatement(goCommands),
      converter.convertStatement(postRunCommands),
      converter.convertStatement(postExperimentCommands),
      repetitions,
      sequentialRunOrder,
      runMetricsEveryStep,
      converter.convertStatement(runMetricsCondition),
      timeLimit,
      if (exitCondition == "") "" else converter.convertReporterExpression(exitCondition),
      metrics.map(converter.convertReporterExpression),
      constants,
      subExperiments
    )
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
