
// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.fileformat
import org.nlogo.api.{ ComponentSerialization, NLogoFormat }
import org.nlogo.core.{ Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader

class NLogoHubNetFormat(manager: GUIHubNetManager) extends ComponentSerialization[GUIHubNetManager, Array[String], NLogoFormat] {
  def componentName: String = "org.nlogo.modelsection.hubnetclient"
  def default: GUIHubNetManager = manager
  def serialize(mgr: GUIHubNetManager): Array[String] =
    mgr.getComponent.map(w => WidgetReader.format(w, fileformat.hubNetReaders).lines.toSeq :+ "").flatten.toArray
  def validationErrors(mgr: GUIHubNetManager): Option[String] = None
  def deserialize(widgets: Array[String]): GUIHubNetManager =
    manager
}
