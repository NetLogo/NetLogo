// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.{ Dialog, Dimension, GridBagConstraints, GridBagLayout, Insets, Window }
import javax.swing.{ JDialog, JPanel, ScrollPaneConstants, Timer, WindowConstants }
import javax.swing.border.{ EmptyBorder, LineBorder }

import org.nlogo.api.{ Dump, ExportPlotWarningAction, LabProtocol, PeriodicUpdateDelay }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.nvm.Workspace
import org.nlogo.plot.DummyPlotManager
import org.nlogo.swing.{ Button, CheckBox, OptionPane, RichAction, ScrollPane, TextArea, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ GUIWorkspace, PlotWidget, SpeedSliderPanel }

private [gui] class ProgressDialog(parent: Window, supervisor: Supervisor,
                                   saveProtocol: (LabProtocol) => Unit)
              extends JDialog(parent, Dialog.DEFAULT_MODALITY_TYPE) with ProgressListener with ThemeSync {
  val protocol = supervisor.worker.protocol
  val workspace = supervisor.workspace.asInstanceOf[GUIWorkspace]
  private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace.progressDialog")
  private val totalRuns = protocol.countRuns
  private val progressArea = new TextArea(10 min (protocol.valueSets(0).size + 3), 0)
  private val scrollPane = new ScrollPane(progressArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  private val timer = new Timer(PeriodicUpdateDelay.DelayInMilliseconds, periodicUpdateAction)
  private val displaySwitch = new CheckBox(displaySwitchAction)
  private val plotsAndMonitorsSwitch = new CheckBox(plotsAndMonitorsSwitchAction)
  private val pauseAction = RichAction(I18N.gui("pause")) { _ =>
    if (!supervisor.paused)
      supervisor.pause()
      pause()
  }
  private val pauseButton = new Button(pauseAction)
  private val abortAction = RichAction(I18N.gui.get("tools.behaviorSpace.abort")) { _ =>
    supervisor.abort()
  }
  private val abortButton = new Button(abortAction)
  private val speedSlider = new SpeedSliderPanel(workspace)

  private var updatePlots = false
  private var started = 0L
  private var runCount = 0
  private var elapsed = "0:00:00"
  private var settingsString = ""
  private var steps = 0

  private val plotWidgetOption: Option[PlotWidget] = {
    if ((protocol.runMetricsEveryStep || !protocol.runMetricsCondition.isEmpty) && protocol.metrics.length > 0) {
      // don't use the real plot manager here, use a dummy one.
      // fixes http://trac.assembla.com/nlogo/ticket/1259
      // the reason for this is that plots normally get added to the plot manager
      // then when clear-all is called (and other things) on the plots
      // in the model, things (such as clearing, which removes temporary pens)
      // would happen to this plot too. but we don't want that.
      // this plot only has temporary pens, in fact.
      // anyway, the point is that things happening in the model should not
      // cause anything to happen to this plot.
      // except of course, for the measurements that this plot is displaying.
      // JC - 4/4/11
      val plotWidget = PlotWidget(I18N.gui("plot.title"), new DummyPlotManager)
      plotWidget.plot.defaultXMin = 0
      plotWidget.plot.defaultYMin = 0
      plotWidget.plot.defaultXMax = 1
      plotWidget.plot.defaultYMax = 1
      plotWidget.plot.defaultAutoPlotOn = true
      plotWidget.xLabel(I18N.gui("plot.time"))
      plotWidget.yLabel(I18N.gui("plot.behavior"))
      plotWidget.clear()
      plotWidget.plot.pens=Nil // make sure to start with no pens. plotWidget adds one by default.
      plotWidget.togglePenList()
      Some(plotWidget)
    }
    else None
  }

  locally {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    addWindowListener(new java.awt.event.WindowAdapter {
      override def windowClosing(e: java.awt.event.WindowEvent): Unit = { abortAction.actionPerformed(null) }
    })
    setTitle(I18N.gui("title", protocol.name))
    setResizable(true)
    getContentPane.setLayout(new GridBagLayout)
    val c = new GridBagConstraints

    c.gridwidth = GridBagConstraints.REMAINDER
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(6, 6, 0, 6)

    getContentPane.add(speedSlider, c)

    c.weighty = 1.0

    plotWidgetOption.foreach{ plotWidget =>
      getContentPane.add(plotWidget, c)
    }

    progressArea.setEditable(false)
    progressArea.setBorder(new EmptyBorder(5, 5, 5, 5))

    getContentPane.add(scrollPane, c)
    updateProgressArea(true)
    scrollPane.setMinimumSize(scrollPane.getPreferredSize())

    c.weighty = 0.0
    c.fill = GridBagConstraints.HORIZONTAL
    getContentPane.add(displaySwitch, c)

    c.insets = new Insets(0, 6, 0, 6)
    getContentPane.add(plotsAndMonitorsSwitch, c)

    val buttonPanel = new JPanel with Transparent

    buttonPanel.add(pauseButton)
    buttonPanel.add(abortButton)

    c.fill = GridBagConstraints.NONE
    c.anchor = GridBagConstraints.EAST
    c.insets = new Insets(6, 6, 6, 6)

    getContentPane.add(buttonPanel, c)

    timer.start()

    pack()

    Positioning.center(this, parent)
  }

  override def getMinimumSize = getPreferredSize
  override def getPreferredSize = new Dimension(super.getPreferredSize.width max 450, super.getPreferredSize.height)

  def pause(): Unit = {
    timer.stop()
    pauseAction.setEnabled(false)
    abortAction.setEnabled(false)
    progressArea.setText(I18N.gui("waiting"))
  }
  lazy val periodicUpdateAction = RichAction(I18N.gui("updateTime")) { _ =>
    updateProgressArea(false)
    plotWidgetOption.foreach{ plotWidget => if (updatePlots) plotWidget.handle(null) }
  }
  lazy val displaySwitchAction = RichAction(I18N.gui("updateView")) { e =>
    workspace.displaySwitchOn(e.getSource.asInstanceOf[CheckBox].isSelected)
  }
  lazy val plotsAndMonitorsSwitchAction = RichAction(I18N.gui("updatePlotsAndMonitors")) { e =>
    updatePlots = e.getSource.asInstanceOf[CheckBox].isSelected
    if (updatePlots) workspace.setPeriodicUpdatesEnabled(true)
    else {
      workspace.setPeriodicUpdatesEnabled(false)
      workspace.jobManager.finishSecondaryJobs(null)
    }
  }

  def saveProtocolP(): Unit = {
    saveProtocol(protocol.copy(runsCompleted = supervisor.highestCompleted, runOptions = supervisor.options))
  }

  def resetProtocol(): Unit = {
    saveProtocol(protocol.copy(runsCompleted = 0, runOptions = null))
  }

  def updateView(check: Boolean): Unit = {
    displaySwitch.setSelected(check)
    workspace.displaySwitchOn(check)
  }

  def setUpdateView(status: Boolean): Unit = {
    updateView(status)
  }

  def plotsAndMonitorsSwitch(check: Boolean): Unit = {
    plotsAndMonitorsSwitch.setSelected(check)
    workspace.setPeriodicUpdatesEnabled(check)
    if (!check) {
      workspace.jobManager.finishSecondaryJobs(null)
    }
  }

  def setPlotsAndMonitorsSwitch(status: Boolean): Unit = {
    plotsAndMonitorsSwitch(status)
    updatePlots = status
  }

  def enablePlotsAndMonitorsSwitch(enabled: Boolean): Unit = {
    plotsAndMonitorsSwitch.setEnabled(enabled)
  }

  def close(): Unit = {
    timer.stop()
    setVisible(false)
    dispose()
    workspace.displaySwitchOn(true)
    workspace.setPeriodicUpdatesEnabled(true)
  }

  def writing(): Unit = {
    timer.stop()
    progressArea.setText(I18N.gui("writing"))
  }

  override def setVisible(visible: Boolean): Unit = {
    syncTheme()

    super.setVisible(visible)
  }

  /// ProgressListener implementation

  override def experimentStarted(): Unit = {started = System.currentTimeMillis}
  override def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]): Unit = {
    if (!w.isHeadless) {
      runCount = runNumber
      steps = 0
      resetPlot()
      settingsString = ""
      for ((name, value) <- settings)
        settingsString += name + " = " + Dump.logoObject(value.asInstanceOf[AnyRef]) + "\n"
      updateProgressArea(true)
    }
  }
  override def stepCompleted(w: Workspace, steps: Int): Unit = {
    if (!w.isHeadless) {
      this.steps = steps
      if (workspace.triedToExportPlot && workspace.exportPlotWarningAction == ExportPlotWarningAction.Warn) {
        workspace.setExportPlotWarningAction(ExportPlotWarningAction.Ignore)
        org.nlogo.awt.EventQueue.invokeLater(new Runnable() {
          def run(): Unit = {
            new OptionPane(workspace.getFrame, I18N.gui("updatingPlotsWarningTitle"),
                           I18N.shared.get("tools.behaviorSpace.runoptions.updateplotsandmonitors.error"),
                           OptionPane.Options.Ok, OptionPane.Icons.Warning)
          }
        })
      }
    }
  }
  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]): Unit = {
    if (!w.isHeadless) plotNextPoint(values)
  }

  private def invokeAndWait(f: => Unit) =
    try org.nlogo.awt.EventQueue.invokeAndWait(new Runnable {def run(): Unit = {f}})
    catch {
      case ex: InterruptedException =>
        // we may get interrupted if the user aborts the run - ST 10/30/03
        org.nlogo.api.Exceptions.ignore(ex)
    }

  private def resetPlot(): Unit = {
    plotWidgetOption.foreach{ plotWidget => invokeAndWait {
      plotWidget.clear()
      for (metricNumber <- 0 until protocol.metrics.length) yield {
        val pen = plotWidget.plot.createPlotPen(getPenName(metricNumber), true)
        pen.color = org.nlogo.api.Color.getColor(Double.box(metricNumber % 14 * 10 + 5)).getRGB
        pen
      }
    }}
  }

  // this is only called when we KNOW we have a plot, so plotWidgetOption.get is ok
  private def getPenName(metricNumber: Int): String = {
    val buf = new StringBuilder()
    if (protocol.metrics.length > 1) buf.append(metricNumber + " ")
    buf.append(org.nlogo.awt.Fonts.shortenStringToFit(
      protocol.metrics(metricNumber).trim.replaceAll("\\s+", " "),
      100, // an arbitrary limit to keep the pen names from getting too wide
      plotWidgetOption.get.getFontMetrics(plotWidgetOption.get.getFont)))
    buf.toString
  }

  private def plotNextPoint(measurements: List[AnyRef]): Unit = {
    plotWidgetOption.foreach { plotWidget => invokeAndWait {
      for (metricNumber <- 0 until protocol.metrics.length) {
        val measurement = measurements(metricNumber)
        if (measurement.isInstanceOf[Number]) {
          val pen = plotWidget.plot.getPen(getPenName(metricNumber)).get
          pen.plot(steps, measurement.asInstanceOf[java.lang.Double].doubleValue)
        }
      }
      plotWidget.plot.makeDirty()
    }}
  }

  private def updateProgressArea(force: Boolean): Unit = {
    def pad(s: String) = if (s.length == 1) ("0" + s) else s
    val elapsedMillis: Int = ((System.currentTimeMillis - started) / 1000).toInt
    val hours = (elapsedMillis / 3600).toString
    val minutes = pad(((elapsedMillis % 3600) / 60).toString)
    val seconds = pad((elapsedMillis % 60).toString)
    val newElapsed = hours + ":" + minutes + ":" + seconds
    if (force || elapsed != newElapsed) {
      elapsed = newElapsed
      org.nlogo.awt.EventQueue.invokeLater(new Runnable {
        def run(): Unit = {
          progressArea.setText(I18N.gui("progressArea", runCount.toString,
            totalRuns.toString, steps.toString, elapsed, settingsString))
          progressArea.setCaretPosition(0)
        }
      })
    }
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground)

    progressArea.syncTheme()

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable))
    scrollPane.setBackground(InterfaceColors.textAreaBackground)

    displaySwitch.setForeground(InterfaceColors.dialogText)
    plotsAndMonitorsSwitch.setForeground(InterfaceColors.dialogText)

    speedSlider.syncTheme()

    pauseButton.syncTheme()
    abortButton.syncTheme()

    plotWidgetOption.foreach(_.syncTheme())
  }
}
