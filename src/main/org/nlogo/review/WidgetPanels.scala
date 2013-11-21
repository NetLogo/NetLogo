// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun
import org.nlogo.widget.NoteWidget
import org.nlogo.widget.SwitchWidget
import org.nlogo.window.ButtonWidget
import org.nlogo.window.ChooserWidget
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.MonitorWidget
import org.nlogo.window.PlotWidget
import org.nlogo.window.SliderWidget
import org.nlogo.window.WidgetContainer

import javax.swing.JPanel

object WidgetPanels {

  def create(ws: GUIWorkspace, run: ModelRun): Seq[JPanel] = {
    val container = ws.viewWidget.findWidgetContainer
    newViewWidgetPanel(ws, container, run) +:
      workspaceWidgets(ws).zipWithIndex.collect {
        case (w: PlotWidget, _) => newPlotPanel(container, w, run)
        case (w: MonitorWidget, i) => newMonitorPanel(container, w, run, i)
        case (w: ButtonWidget, _) => newButtonPanel(container, w)
        case (w: NoteWidget, _) => newNotePanel(container, w)
        case (w: SwitchWidget, i) => newSwitchPanel(container, w, run, i)
        case (w: SliderWidget, i) => newSliderPanel(container, w, run, i)
        case (w: ChooserWidget, i) => newChooserPanel(container, w, run, i)
        //        case (w: InputBoxWidget, _) =>
      }
  }

  def newViewWidgetPanel(
    ws: GUIWorkspace,
    container: WidgetContainer,
    run: ModelRun): ViewWidgetPanel = {
    val viewSettings = ReviewTabViewSettings(ws.view)
    val viewWidgetBounds = container.getUnzoomedBounds(ws.viewWidget)
    new ViewWidgetPanel(
      run,
      viewWidgetBounds,
      viewSettings,
      ws.world.worldHeight,
      ws.viewWidget.getInsideBorderHeight,
      ws.view.unzoomedFont)
  }

  def newMonitorPanel(
    container: WidgetContainer,
    monitorWidget: MonitorWidget,
    run: ModelRun,
    index: Int): MonitorPanel = {
    new MonitorPanel(
      container.getUnzoomedBounds(monitorWidget),
      monitorWidget.originalFont,
      monitorWidget.displayName,
      run,
      index)
  }

  def newPlotPanel(
    container: WidgetContainer,
    plotWidget: PlotWidget,
    run: ModelRun): PlotPanel = {
    new PlotPanel(
      container.getUnzoomedBounds(plotWidget),
      plotWidget.originalFont,
      run,
      plotWidget.plot,
      plotWidget.gui.legend.open,
      plotWidget.gui.xAxis.getLabel,
      plotWidget.gui.yAxis.getLabel)
  }

  def newButtonPanel(
    container: WidgetContainer,
    buttonWidget: ButtonWidget): ButtonPanel =
    new ButtonPanel(
      container.getUnzoomedBounds(buttonWidget),
      buttonWidget.originalFont,
      buttonWidget.actionKeyString,
      buttonWidget.buttonType,
      buttonWidget.displayName,
      buttonWidget.forever)

  def newNotePanel(
    container: WidgetContainer,
    noteWidget: NoteWidget): NotePanel =
    new NotePanel(
      container.getUnzoomedBounds(noteWidget),
      noteWidget.originalFont,
      noteWidget.text,
      noteWidget.color)

  def newSwitchPanel(
    container: WidgetContainer,
    switchWidget: SwitchWidget,
    run: ModelRun,
    index: Int): SwitchPanel = {
    new SwitchPanel(
      container.getUnzoomedBounds(switchWidget),
      switchWidget.originalFont,
      switchWidget.displayName,
      run,
      index)
  }

  def newSliderPanel(
    container: WidgetContainer,
    sliderWidget: SliderWidget,
    run: ModelRun,
    index: Int): SliderPanel = {
    val sliderPanel = new SliderPanel(
      container.getUnzoomedBounds(sliderWidget),
      run,
      index)
    sliderPanel.name = sliderWidget.name
    sliderPanel.setSliderConstraint(sliderWidget.constraint)
    sliderPanel.units = sliderWidget.units
    sliderPanel.vertical = sliderWidget.vertical
    sliderPanel.removeAllListeners() // needs to be called AFTER setting `vertical`
    sliderPanel
  }

  def newChooserPanel(
    container: WidgetContainer,
    chooserWidget: ChooserWidget,
    run: ModelRun,
    index: Int): ChooserPanel = {
    new ChooserPanel(
      container.getUnzoomedBounds(chooserWidget),
      chooserWidget.originalFont,
      chooserWidget.getMargin,
      chooserWidget.name,
      run,
      index)
  }
}
