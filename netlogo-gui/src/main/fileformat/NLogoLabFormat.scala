// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ LiteralParser, Model }
import org.nlogo.api.LabProtocol
import org.nlogo.api.ComponentSerialization

class NLogoLabFormat(autoConvert: String => String => String, literalParser: LiteralParser)
  extends ComponentSerialization[Array[String], NLogoFormat] {
  def componentName = "org.nlogo.modelsection.behaviorspace"

  val loader = new LabLoader(literalParser)

  override def addDefault = { (m: Model) =>
    m.withOptionalSection(componentName, None, Seq[LabProtocol]()) }

  def serialize(m: Model): Array[String] = {
    m.optionalSectionValue[Seq[LabProtocol]](componentName)
      .map(ps => LabSaver.save(ps).lines.toArray)
      .getOrElse(Array[String]())
  }

  def validationErrors(m: Model) =
    None

  def autoConvertProtocol(versionOpt: Option[String])(protocol:LabProtocol): LabProtocol = {
    import protocol._
    versionOpt.map(version =>
        new LabProtocol(name,
          autoConvert(version)(setupCommands),
          autoConvert(version)(goCommands),
          autoConvert(version)(finalCommands),
          repetitions, runMetricsEveryStep, timeLimit,
          autoConvert(version)(exitCondition),
          metrics.map(autoConvert(version)),
          valueSets)).getOrElse(protocol)
  }

  override def deserialize(s: Array[String]) = {(m: Model) =>
    m.withOptionalSection(componentName, load(s, Some(m.version)), Seq[LabProtocol]())
  }

  def load(s: Array[String], version: Option[String]): Option[Seq[LabProtocol]] =
    if (s.isEmpty || s.forall(_.isEmpty)) None
    else Some(loader(s.mkString("\n")).map(autoConvertProtocol(version)))
  }
