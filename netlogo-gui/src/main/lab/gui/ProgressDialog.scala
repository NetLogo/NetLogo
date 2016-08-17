// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.swing.RichAction
import org.nlogo.nvm.Workspace
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.window.{DisplaySwitch, PlotWidget, ViewUpdatePanel}
import javax.swing.ScrollPaneConstants._
import javax.swing._
import java.awt.Dimension
import org.nlogo.api.{PeriodicUpdateDelay, Dump}
import org.nlogo.plot.DummyPlotManager

private [gui] class ProgressDialog(dialog: java.awt.Dialog, supervisor: Supervisor)
              extends JDialog(dialog, true) with ProgressListener{

  val protocol = supervisor.worker.protocol
  val workspace = supervisor.workspace
  private val totalRuns = protocol.countRuns
  private val progressArea = new JTextArea(10 min (protocol.valueSets.size + 3), 0)
  private val timer = new Timer(PeriodicUpdateDelay.DelayInMilliseconds, periodicUpdateAction)

  private var updatePlots = true
  private var started = 0L
  private var runCount = 0
  private var elapsed = "0:00:00"
  private var settingsString = ""
  private var steps = 0

  private val plotWidgetOption: Option[PlotWidget] = {
    if (protocol.runMetricsEveryStep && protocol.metrics.length > 0) {
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
      val plotWidget = PlotWidget("Behavior Plot", new DummyPlotManager)
      plotWidget.plot.defaultXMin = 0
      plotWidget.plot.defaultYMin = 0
      plotWidget.plot.defaultXMax = 1
      plotWidget.plot.defaultYMax = 1
      plotWidget.plot.defaultAutoPlotOn = true
      plotWidget.xLabel("Time")
      plotWidget.yLabel("Behavior")
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
      override def windowClosing(e: java.awt.event.WindowEvent) { abortAction.actionPerformed(null) }
    })
    setTitle("Running Experiment: " + protocol.name)
    setResizable(true)
    val layout = new java.awt.GridBagLayout
    getContentPane.setLayout(layout)
    val c = new java.awt.GridBagConstraints
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER
    c.fill = java.awt.GridBagConstraints.BOTH
    c.weightx = 1.0
    c.weighty = 0.0
    c.insets = new java.awt.Insets(6, 6, 0, 6)
    val ticksPanel = new ViewUpdatePanel(workspace, new DisplaySwitch(workspace), false, workspace.viewWidget.tickCounter)
    layout.setConstraints(ticksPanel, c)
    getContentPane.add(ticksPanel)
    c.weighty = 1.0

    plotWidgetOption.foreach{ plotWidget =>
      layout.setConstraints(plotWidget, c)
      getContentPane.add(plotWidget)
    }

    progressArea.setEditable(false)
    progressArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    val scrollPane = new JScrollPane(progressArea, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED)
    layout.setConstraints(scrollPane, c)
    getContentPane.add(scrollPane)
    updateProgressArea(true)
    scrollPane.setMinimumSize(scrollPane.getPreferredSize())

    c.weighty = 0.0
    val displaySwitch = new JCheckBox(displaySwitchAction)
    displaySwitch.setSelected(true)
    workspace.displaySwitchOn(true)
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    layout.setConstraints(displaySwitch, c)
    getContentPane.add(displaySwitch)

    val plotsAndMonitorsSwitch = new JCheckBox(plotsAndMonitorsSwitchAction)
    plotsAndMonitorsSwitch.setSelected(true)
    c.insets = new java.awt.Insets(0, 6, 0, 6)
    layout.setConstraints(plotsAndMonitorsSwitch, c)
    getContentPane.add(plotsAndMonitorsSwitch)

    val abortButton = new JButton(abortAction)
    c.fill = java.awt.GridBagConstraints.NONE
    c.anchor = java.awt.GridBagConstraints.EAST
    c.insets = new java.awt.Insets(6, 6, 6, 6)
    layout.setConstraints(abortButton, c)
    getContentPane.add(abortButton)

    timer.start()

    pack()
    org.nlogo.awt.Positioning.center(this, dialog)
  }

  override def getMinimumSize = getPreferredSize
  override def getPreferredSize = new Dimension(super.getPreferredSize.width max 450, super.getPreferredSize.height)

  // The following actions are declared lazy so we can use them in the
  // initialization code above.  Two cheers for Scala. - ST 11/12/08

  lazy val abortAction = RichAction("Abort") { _ =>
    supervisor.interrupt()
    close()
  }
  lazy val periodicUpdateAction = RichAction("update elapsed time") { _ =>
    updateProgressArea(false)
    plotWidgetOption.foreach{ plotWidget => if(updatePlots) plotWidget.handle(null) }
  }
  lazy val displaySwitchAction = RichAction("Update view") { e =>
    workspace.displaySwitchOn(e.getSource.asInstanceOf[JCheckBox].isSelected)
  }
  lazy val plotsAndMonitorsSwitchAction = RichAction("Update plots and monitors") { e =>
    updatePlots = e.getSource.asInstanceOf[JCheckBox].isSelected
    if (updatePlots) workspace.setPeriodicUpdatesEnabled(true)
    else {
      workspace.setPeriodicUpdatesEnabled(false)
      workspace.jobManager.finishSecondaryJobs(null)
    }
  }
  def close() {
    timer.stop()
    setVisible(false)
    dispose()
    workspace.displaySwitchOn(true)
    workspace.setPeriodicUpdatesEnabled(true)
  }

  /// ProgressListener implementation

  override def experimentStarted() {started = System.currentTimeMillis}
  override def runStarted(w: Workspace, runNumber: Int, settings: List[(String, Any)]) {
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
  override def stepCompleted(w: Workspace, steps: Int) {
    if(!w.isHeadless) this.steps = steps
  }
  override def measurementsTaken(w: Workspace, runNumber: Int, step: Int, values: List[AnyRef]) {
    if(!w.isHeadless) plotNextPoint(values)
  }

  private def invokeAndWait(f: => Unit) =
    try org.nlogo.awt.EventQueue.invokeAndWait(new Runnable {def run() {f}})
    catch {
      case ex: InterruptedException =>
        // we may get interrupted if the user aborts the run - ST 10/30/03
        org.nlogo.api.Exceptions.ignore(ex)
    }

  private def resetPlot() {
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

  private def plotNextPoint(measurements: List[AnyRef]) {
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

  private def updateProgressArea(force: Boolean) {
    def pad(s: String) = if (s.length == 1) ("0" + s) else s
    val elapsedMillis: Int = ((System.currentTimeMillis - started) / 1000).toInt
    val hours = (elapsedMillis / 3600).toString
    val minutes = pad(((elapsedMillis % 3600) / 60).toString)
    val seconds = pad((elapsedMillis % 60).toString)
    val newElapsed = hours + ":" + minutes + ":" + seconds
    if (force || elapsed != newElapsed) {
      elapsed = newElapsed
      org.nlogo.awt.EventQueue.invokeLater(new Runnable {
        def run() {
          progressArea.setText("Run #" + runCount + " of " + totalRuns + ", " +
                  "step #" + steps + "\n" +
                  "Total elapsed time: " + elapsed + "\n" + settingsString)
          progressArea.setCaretPosition(0)
        }
      })
    }
  }
}
