// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.ComponentSerialization
import org.nlogo.core.{ Model, Widget => CoreWidget }
import org.nlogo.core.model.{ Element, ElementFactory, HubNetWidgetXml }

class NLogoXHubNetFormat(val factory: ElementFactory)
  extends ComponentSerialization[NLogoXFormat.Section, NLogoXFormat]
  with NLogoXBaseReader {

  def componentName: String = "org.nlogo.modelsection.hubnetclient"

  override def addDefault = { (m: Model) =>
    m.withOptionalSection[Seq[CoreWidget]](componentName, None, Seq[CoreWidget]())
  }

  def serialize(m: Model): Element =
    toSeqElement[CoreWidget]("hubnet",
      m.optionalSectionValue[Seq[CoreWidget]](componentName).getOrElse(List.empty[CoreWidget]),
      (w: CoreWidget) => HubNetWidgetXml.write(w, factory))

  def validationErrors(m: Model): Option[String] = None

  override def deserialize(e: Element) = { (m: Model) =>
    parseChildren(e, HubNetWidgetXml.read _)
      .map(widgets => m.withOptionalSection(componentName, Some(widgets), Seq.empty[CoreWidget]))
  }
}
