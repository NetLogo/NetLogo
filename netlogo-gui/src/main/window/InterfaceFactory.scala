// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.ImageIcon
import org.nlogo.api.I18N

object WidgetInfo {
  def apply(widgetType: String, imageName: String): WidgetInfo = {
    implicit val i18nPrefix = I18N.Prefix("tabs.run.widgets")
    WidgetInfo(I18N.gui(widgetType.toLowerCase), widgetType.toUpperCase, imageName)
  }
  val button = WidgetInfo("button", "button.gif")
  val slider = WidgetInfo("slider", "slider.gif")
  val switch = WidgetInfo("switch", "switch.gif")
  val chooser = WidgetInfo("chooser", "chooser.gif")
  val input = WidgetInfo("input", "input.gif")
  val monitor = WidgetInfo("monitor", "monitor.gif")
  val plot = WidgetInfo("plot", "plot.gif")
  val output = WidgetInfo("output", "output.gif")
  val note = WidgetInfo("note", "note.gif")
  val view = WidgetInfo("view", "view.gif")
}

case class WidgetInfo(displayName: String, widgetType: String, imageName: String) {
  def icon = new ImageIcon(classOf[WidgetInfo].getResource("/images/" + imageName))
}

trait InterfaceFactory {
  def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel

  def toolbar(interfacePanel: AbstractWidgetPanel,
              workspace: GUIWorkspace,
              buttons: List[WidgetInfo], frame: java.awt.Frame): java.awt.Component
}
