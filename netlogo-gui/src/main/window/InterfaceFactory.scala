// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame

import org.nlogo.core.{ LocaleContext, View => CoreView, Widget => CoreWidget,
  Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Output => CoreOutput, Plot => CorePlot, Slider => CoreSlider,
  Switch => CoreSwitch, TextBox => CoreTextBox }
import org.nlogo.swing.{ Utils, ZoomHelpers }

object WidgetInfo {
  def apply(widgetType: String, imageName: String, widgetThunk: () => CoreWidget): WidgetInfo = {
    WidgetInfo(widgetType, widgetThunk, imageName)
  }

  val button  = WidgetInfo(loc"Button", "button.png", () => CoreButton(None, 0, 0, 0, 0))
  val slider  = WidgetInfo(loc"Slider", "slider.png", () => CoreSlider(None))
  val switch  = WidgetInfo(loc"Switch", "switch.png", () => CoreSwitch(None))
  val chooser = WidgetInfo(loc"Chooser", "chooser.png", () => CoreChooser(None))
  val input   = WidgetInfo(loc"Input", "input.png", () => CoreInputBox(None))
  val monitor = WidgetInfo(loc"Monitor", "monitor.png", () => CoreMonitor(None, 0, 0, 0, 0, false, None, 10))
  val plot    = WidgetInfo(loc"Plot", "plot.png", () => CorePlot(None))
  val output  = WidgetInfo(loc"Output", "output.png", () => CoreOutput(0, 0, 0, 0, 13))
  val note    = WidgetInfo(loc"Note", "note.png", () => CoreTextBox(None, fontSize = 11))
  val view    = WidgetInfo(loc"View", "view.gif", () => CoreView())

  val primaryInfos: Seq[WidgetInfo] = Seq(button, slider, switch, chooser, input, monitor, plot, output, note)
  val hubNetInfos: Seq[WidgetInfo] = Seq(button, slider, switch, chooser, input, monitor, plot, note, view)
}

case class WidgetInfo(displayName: String, widgetThunk: () => CoreWidget, imageName: String) {
  def icon(zoom: ZoomHelpers) = Utils.iconScaled(zoom, "/images/" + imageName, 27, 16)
  def coreWidget = widgetThunk()
}

trait InterfaceFactory {
  def widgetPanel(frame: Frame, workspace: GUIWorkspace, widgetInfos: Seq[WidgetInfo]): AbstractWidgetPanel
}
