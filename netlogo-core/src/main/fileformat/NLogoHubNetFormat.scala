// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.ComponentSerialization
import org.nlogo.core.{ LiteralParser, Model, Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader

import scala.util.Try

class NLogoHubNetFormat(literalParser: LiteralParser)
  extends ComponentSerialization[Array[String], NLogoFormat] {
  def componentName: String = "org.nlogo.modelsection.hubnetclient"

  override def addDefault = { (m: Model) =>
    m.withOptionalSection[Seq[CoreWidget]](componentName, None, Seq[CoreWidget]())
  }

  def serialize(m: Model): Array[String] =
    m.optionalSectionValue[Seq[CoreWidget]](componentName)
      .map(_.map(w => WidgetReader.format(w, hubNetReaders).lines.toSeq :+ "").flatten.toArray[String])
      .getOrElse(Array[String]())

  def validationErrors(m: Model): Option[String] = None

  override def deserialize(widgetLines: Array[String]) = { (m: Model) =>
    Try {
      val widgets =
        WidgetReader.readInterface(widgetLines.toList, literalParser, hubNetReaders, identity)
      m.withOptionalSection(componentName, Some(widgets), Seq[CoreWidget]())
    }
  }
}
