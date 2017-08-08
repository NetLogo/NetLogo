// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ LiteralParser, Model }
import org.nlogo.api.{ LabProtocol, ModelFormat, ComponentSerialization }

import scala.util.Try

trait LabFormat {
  def componentName = "org.nlogo.modelsection.behaviorspace"

  def validationErrors(m: Model) =
    None
}

trait AbstractNLogoLabFormat[A <: ModelFormat[Array[String], A]]
  extends ComponentSerialization[Array[String], A]
  with LabFormat {

  def literalParser: LiteralParser

  val loader = new LabLoader(literalParser)

  override def addDefault = { (m: Model) =>
    m.withOptionalSection(componentName, None, Seq[LabProtocol]()) }

  def serialize(m: Model): Array[String] = {
    m.optionalSectionValue[Seq[LabProtocol]](componentName)
      .map(ps => if (ps.isEmpty) Array[String]() else LabSaver.save(ps).lines.toArray)
      .getOrElse(Array[String]())
  }

  override def deserialize(s: Array[String]) = {(m: Model) =>
    Try { m.withOptionalSection(componentName, load(s, Some(m.version)), Seq[LabProtocol]()) }
  }

  def load(s: Array[String], version: Option[String]): Option[Seq[LabProtocol]] =
    if (s.isEmpty || s.forall(_.isEmpty)) None
    else Some(loader(s.mkString("\n")))
}

class NLogoLabFormat(val literalParser: LiteralParser)
  extends AbstractNLogoLabFormat[NLogoFormat]

class NLogoThreeDLabFormat(val literalParser: LiteralParser)
  extends AbstractNLogoLabFormat[NLogoThreeDFormat]

