// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ LiteralParser, Model }
import org.nlogo.api.{ AutoConverter, LabProtocol, ModelFormat, ComponentSerialization }

import scala.util.Try

import scala.util.Try

trait LabFormat[A <: ModelFormat[Array[String], A]]
  extends ComponentSerialization[Array[String], A] {

  def literalParser: LiteralParser

  def componentName = "org.nlogo.modelsection.behaviorspace"

  val loader = new LabLoader(literalParser)

  override def addDefault = { (m: Model) =>
    m.withOptionalSection(componentName, None, Seq[LabProtocol]()) }

  def serialize(m: Model): Array[String] = {
    m.optionalSectionValue[Seq[LabProtocol]](componentName)
      .map(ps => if (ps.isEmpty) Array[String]() else LabSaver.save(ps).lines.toArray)
      .getOrElse(Array[String]())
  }

  def validationErrors(m: Model) =
    None

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
      repetitions, runMetricsEveryStep, timeLimit,
      if (exitCondition == "") "" else converter.convertReporterExpression(exitCondition),
      metrics.map(converter.convertReporterExpression),
      valueSets)
  }

  override def deserialize(s: Array[String]) = {(m: Model) =>
    Try { m.withOptionalSection(componentName, load(s, Some(m.version)), Seq[LabProtocol]()) }
  }

  override def requiresAutoConversion(model: Model, needsConversion: String => Boolean): Boolean =
    model.optionalSectionValue[Seq[LabProtocol]](componentName)
      .exists(protocols => protocols.exists(needingConversion(needsConversion, _)))


  override def autoConvert(model: Model, converter: AutoConverter): Model = {
    if (model.hasValueForOptionalSection(componentName))
      model.optionalSectionValue[Seq[LabProtocol]](componentName).map(protocols =>
          model.withOptionalSection(componentName, Some(protocols.map(autoConvertProtocol(converter))), Seq())
        ).getOrElse(model)
    else
      model
  }

  def load(s: Array[String], version: Option[String]): Option[Seq[LabProtocol]] =
    if (s.isEmpty || s.forall(_.isEmpty)) None
    else Some(loader(s.mkString("\n")))
}

class NLogoLabFormat(val literalParser: LiteralParser)
  extends LabFormat[NLogoFormat]

class NLogoThreeDLabFormat(val literalParser: LiteralParser)
  extends LabFormat[NLogoThreeDFormat]

