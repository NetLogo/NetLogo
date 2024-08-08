// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Image
import javax.swing.ImageIcon

import org.nlogo.core.I18N
import org.nlogo.core.{ I18N, View => CoreView, Widget => CoreWidget,
  Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Output => CoreOutput, Plot => CorePlot, Slider => CoreSlider,
  Switch => CoreSwitch, TextBox => CoreTextBox }
import org.nlogo.swing.Utils

object WidgetInfo {
  def apply(widgetType: String, imageName: String, widgetThunk: () => CoreWidget): WidgetInfo = {
    implicit val i18nPrefix = I18N.Prefix("tabs.run.widgets")
    WidgetInfo(I18N.gui(widgetType.toLowerCase), widgetThunk, imageName)
  }

  val button  = WidgetInfo("button", "button.png", () => CoreButton(None, 0, 0, 0, 0))
  val slider  = WidgetInfo("slider", "slider.png", () => CoreSlider(None))
  val switch  = WidgetInfo("switch", "switch.png", () => CoreSwitch(None))
  val chooser = WidgetInfo("chooser", "chooser.png", () => CoreChooser(None))
  val input   = WidgetInfo("input", "input.png", () => CoreInputBox(None))
  val monitor = WidgetInfo("monitor", "monitor.png", () => CoreMonitor(None, 0, 0, 0, 0, None, 10))
  val plot    = WidgetInfo("plot", "plot.png", () => CorePlot(None))
  val output  = WidgetInfo("output", "output.png", () => CoreOutput(0, 0, 0, 0, 13))
  val note    = WidgetInfo("note", "note.png", () => CoreTextBox(None, fontSize = 11, color = 0))
  val view    = WidgetInfo("view", "view.gif", () => CoreView())
}

case class WidgetInfo(displayName: String, widgetThunk: () => CoreWidget, imageName: String) {
  def icon = new ImageIcon(Utils.icon("/images/" + imageName).getImage.getScaledInstance(25, 15, Image.SCALE_SMOOTH))
  def coreWidget = widgetThunk()
}

trait InterfaceFactory {
  def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel

  def toolbar(interfacePanel: AbstractWidgetPanel,
              workspace: GUIWorkspace,
              buttons: List[WidgetInfo], frame: java.awt.Frame): java.awt.Component
}
