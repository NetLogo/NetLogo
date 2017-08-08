// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  org.nlogo.core.{ Model, model }, model.ElementFactory

import
  org.nlogo.api.{ ComponentSerialization, LabProtocol }

import
  scala.util.Try

class NLogoXLabFormat(val factory: ElementFactory)
  extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat]
  with NLogoXBaseReader
  with LabFormat {
    override def deserialize(s: NLogoXFormat.Section): Model => Try[Model] = { (m: Model) =>
      parseChildren(s, LabProtocolXml.read _)
        .map(protocols => m.withOptionalSection(componentName, Some(protocols), Seq.empty[LabProtocol]))
    }

  def serialize(m: Model): NLogoXFormat.Section =
    toSeqElement("experiments",
      m.optionalSectionValue[Seq[LabProtocol]](componentName).getOrElse(Seq()),
      (p: LabProtocol) => LabProtocolXml.write(p, factory))
}
