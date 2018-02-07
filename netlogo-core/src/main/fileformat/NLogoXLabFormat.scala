// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  org.nlogo.core.Model

import
  org.nlogo.xmllib.{ Element, ElementFactory }

import
  org.nlogo.api.{ ComponentSerialization, LabProtocol }

import
  scala.util.Try

class NLogoXLabFormat(val factory: ElementFactory)
  extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat]
  with NLogoXBaseReader
  with LabFormat {
    override def deserialize(s: NLogoXFormat.Section): Model => Try[Model] = { (m: Model) =>
      parseChildren(s, ExperimentXml.read _)
        .map(protocols => m.withOptionalSection(componentName, Some(protocols), Seq.empty[LabProtocol]))
    }

  def serialize(m: Model): NLogoXFormat.Section =
    toSeqElement("experiments",
      m.optionalSectionValue[Seq[LabProtocol]](componentName).getOrElse(Seq()),
      (p: LabProtocol) => ExperimentXml.write(p, factory))

  def load(e: Element, version: Option[String]): Option[Seq[LabProtocol]] =
    parseChildren(e, ExperimentXml.read _).toOption
}
