// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.I18N
import org.nlogo.core.{ I18N, View => CoreView, Widget => CoreWidget,
  Button => CoreButton, Chooser => CoreChooser, Image => CoreImage, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Output => CoreOutput, Plot => CorePlot, Slider => CoreSlider,
  Switch => CoreSwitch, TextBox => CoreTextBox }
import org.nlogo.swing.{ Utils => SwingUtils }

object WidgetInfo {
  def apply(widgetType: String, imageName: String, widgetThunk: () => CoreWidget): WidgetInfo = {
    implicit val i18nPrefix = I18N.Prefix("tabs.run.widgets")
    WidgetInfo(I18N.gui(widgetType.toLowerCase), widgetThunk, imageName)
  }

  val button  = WidgetInfo("button", "button.gif", () => CoreButton(None, 0, 0, 0, 0))
  val slider  = WidgetInfo("slider", "slider.gif", () => CoreSlider(None))
  val switch  = WidgetInfo("switch", "switch.gif", () => CoreSwitch(None))
  val chooser = WidgetInfo("chooser", "chooser.gif", () => CoreChooser(None))
  val input   = WidgetInfo("input", "input.gif", () => CoreInputBox(None))
  val monitor = WidgetInfo("monitor", "monitor.gif", () => CoreMonitor(None, 0, 0, 0, 0, None, 10))
  val plot    = WidgetInfo("plot", "plot.gif", () => CorePlot(None))
  val output  = WidgetInfo("output", "output.gif", () => CoreOutput(0, 0, 0, 0, 13))
  val note    = WidgetInfo("note", "note.gif", () => CoreTextBox(None, fontSize = 11, color = 0))
  val image   = WidgetInfo("image", "note.gif", () => CoreImage(0, 0, 0, 0, "")) // placeholder image, change later
  val view    = WidgetInfo("view", "view.gif", () => CoreView())
}

case class WidgetInfo(displayName: String, widgetThunk: () => CoreWidget, imageName: String) {
  def icon = SwingUtils.icon("/images/" + imageName)
  def coreWidget = widgetThunk()
}

trait InterfaceFactory {
  def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel

  def toolbar(interfacePanel: AbstractWidgetPanel,
              workspace: GUIWorkspace,
              buttons: List[WidgetInfo], frame: java.awt.Frame): java.awt.Component
}
