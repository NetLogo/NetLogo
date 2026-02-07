// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.{ Dimension, EventQueue, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JDialog, JPanel, ScrollPaneConstants, Timer, WindowConstants }
import javax.swing.border.{ EmptyBorder, LineBorder }

import org.nlogo.analytics.Analytics
import org.nlogo.api.{ Color, Exceptions, ExportPlotWarningAction, LabProtocol }
import org.nlogo.awt.Fonts
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.nvm.LabInterface
import org.nlogo.plot.DummyPlotManager
import org.nlogo.swing.{ Button, ButtonPanel, CheckBox, OptionPane, Positioning, RichAction, ScrollPane, TextArea,
                         Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ PlotWidget, SpeedSliderPanel }

import scala.concurrent.duration.DurationLong

class ProgressDialog(app: BehaviorSpaceApp, workspace: SemiHeadlessWorkspace, lab: LabInterface,
                     colorizer: Colorizer, protocol: LabProtocol)
  extends JDialog(app.getFrame, I18N.gui.getN("tools.behaviorSpace.progressDialog.title", protocol.name))
  with ThemeSync {

  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.behaviorSpace.progressDialog")

  private val totalRuns = protocol.countRuns

  private val progressArea = new TextArea(10.min(protocol.valueSets.headOption.fold(0)(_.size) + 3), 0) {
    setEditable(false)
    setBorder(new EmptyBorder(6, 6, 6, 6))
  }

  private val scrollPane = new ScrollPane(progressArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    override def getMinimumSize: Dimension =
      this.getPreferredSize
  }

  private val timer = new Timer(1000, RichAction(_ => updateProgressArea()))

  private val updateViewCheckbox = new CheckBox(I18N.gui("updateView"), workspace.setUpdateView)
  private val plotsAndMonitorsSwitch = new CheckBox(I18N.gui("updatePlotsAndMonitors"), checked => {
    workspace.setUpdatePlotsAndMonitors(checked)

    if (!checked)
      workspace.jobManager.finishSecondaryJobs(null)
  })

  private val pauseButton = new Button(I18N.gui("pause"), pause)
  private val abortButton = new Button(I18N.gui.get("tools.behaviorSpace.abort"), app.abort)

  private val speedSlider = new SpeedSliderPanel(workspace)

  private var started = 0L
  private var runCount = 0
  private var settingsString = ""
  private var steps = 0

  private val plotWidgetOption: Option[PlotWidget] = {
    if ((protocol.runMetricsEveryStep || protocol.runMetricsCondition.nonEmpty) && protocol.metrics.nonEmpty) {
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
      val plotManager = new DummyPlotManager
      val plotWidget = new PlotWidget(plotManager.newPlot(I18N.gui("plot.title")), plotManager, workspace, colorizer) {
        // the default plot size assumes there is no legend, so add the legend height
        // to ensure that enough canvas area is visible by default (Isaac B 7/22/25)
        override def getMinimumSize: Dimension =
          new Dimension(super.getMinimumSize.width,
                        (super.getPreferredSize.height - legendHeight).max(100) + legendHeight)

        override def getPreferredSize: Dimension =
          getMinimumSize
      }

      plotManager.compilePlot(plotWidget.plot)

      plotWidget.plot.defaultXMin = 0
      plotWidget.plot.defaultYMin = 0
      plotWidget.plot.defaultXMax = 1
      plotWidget.plot.defaultYMax = 1
      plotWidget.plot.defaultAutoPlotX = true
      plotWidget.plot.defaultAutoPlotY = true
      plotWidget.setXLabel(I18N.gui("plot.time"))
      plotWidget.setYLabel(I18N.gui("plot.behavior"))
      plotWidget.clear()
      plotWidget.togglePenList()

      Some(plotWidget)
    } else {
      None
    }
  }

  locally {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        app.abort()
      }
    })

    setResizable(true)

    add(new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.gridx = 0
      c.weightx = 1
      c.insets = new Insets(6, 6, 6, 6)

      add(speedSlider, c)

      c.weighty = 1
      c.fill = GridBagConstraints.BOTH
      c.insets = new Insets(0, 6, 6, 6)

      plotWidgetOption.foreach { plotWidget =>
        add(plotWidget, c)

        c.weighty = 0
        c.fill = GridBagConstraints.HORIZONTAL
      }

      add(scrollPane, c)

      c.weighty = 0
      c.fill = GridBagConstraints.HORIZONTAL
      c.anchor = GridBagConstraints.WEST

      add(updateViewCheckbox, c)
      add(plotsAndMonitorsSwitch, c)

      c.anchor = GridBagConstraints.CENTER

      add(new ButtonPanel(Seq(pauseButton, abortButton)), c)
    })

    setSize(getMinimumSize)

    Positioning.center(this, app.getFrame)

    updateViewCheckbox.setSelected(workspace.getUpdateView)
    plotsAndMonitorsSwitch.setSelected(workspace.getUpdatePlotsAndMonitors)

    updateProgressArea()

    setVisible(true)
  }

  override def getMinimumSize: Dimension =
    getPreferredSize

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width.max(450), super.getPreferredSize.height)

  def pause(): Unit = {
    timer.stop()
    pauseButton.setEnabled(false)
    abortButton.setEnabled(false)
    progressArea.setText(I18N.gui("waiting"))
    lab.pause()
  }

  def writing(): Unit = {
    timer.stop()
    progressArea.setText(I18N.gui("writing"))
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible) {
      syncTheme()

      Positioning.center(this, app.getFrame)

      Analytics.bspaceRun(protocol.table, protocol.spreadsheet, protocol.stats, protocol.lists)

      timer.start()
    }

    super.setVisible(visible)
  }

  def experimentStarted(): Unit = {
    started = System.currentTimeMillis
  }

  def runStarted(runNumber: Int, settings: Seq[(String, String)]): Unit = {
    runCount = runNumber
    steps = 0
    resetPlot()
    settingsString = settings.map(_ + " = " + _).mkString("\n")

    plotWidgetOption.foreach(_.refreshGUI())

    // the plot pens don't affect the height of the plot until it's invalidated and repainted,
    // so we re-pack window to the correct size here (Isaac B 7/22/25)
    EventQueue.invokeLater(() => {
      pack()
      setSize(getMinimumSize)
    })
  }

  def stepCompleted(step: Int): Unit = {
    steps = step

    updateProgressArea()

    if (workspace.triedToExportPlot && workspace.exportPlotWarningAction == ExportPlotWarningAction.Warn) {
      workspace.setExportPlotWarningAction(ExportPlotWarningAction.Ignore)

      EventQueue.invokeLater(() => {
        new OptionPane(app.getFrame, I18N.gui("updatingPlotsWarningTitle"),
                       I18N.shared.get("tools.behaviorSpace.runoptions.updateplotsandmonitors.error"),
                       OptionPane.Options.Ok, OptionPane.Icons.Warning)
      })
    }
  }

  def measurementsTaken(values: Seq[Double]): Unit = {
    plotNextPoint(values)
  }

  private def invokeAndWait(f: => Unit): Unit = {
    try {
      EventQueue.invokeAndWait(() => f)
    } catch {
      case ex: InterruptedException =>
        // we may get interrupted if the user aborts the run - ST 10/30/03
        Exceptions.ignore(ex)
    }
  }

  private def resetPlot(): Unit = {
    plotWidgetOption.foreach { plotWidget =>
      invokeAndWait {
        plotWidget.clear()

        protocol.metrics.indices.foreach { metric =>
          plotWidget.plot.createPlotPen(getPenName(metric), true).color =
            Color.getColor(Double.box(metric % 14 * 10 + 5)).getRGB
        }
      }
    }
  }

  // this is only called when we KNOW we have a plot, so plotWidgetOption.get is ok
  private def getPenName(metricNumber: Int): String = {
    val name: String = {
      Fonts.shortenStringToFit(
        protocol.metrics(metricNumber).trim.replaceAll("\\s+", " "),
        100, // an arbitrary limit to keep the pen names from getting too wide
        plotWidgetOption.get.getFontMetrics(plotWidgetOption.get.getFont)
      )
    }

    if (protocol.metrics.length > 1) {
      s"$metricNumber $name"
    } else {
      name
    }
  }

  private def plotNextPoint(measurements: Seq[Double]): Unit = {
    if (workspace.getUpdatePlotsAndMonitors) {
      plotWidgetOption.foreach { plotWidget =>
        invokeAndWait {
          protocol.metrics.indices.foreach { metric =>
            plotWidget.plot.getPen(getPenName(metric)).get.plot(steps, measurements(metric))
          }

          plotWidget.plot.makeDirty()
          plotWidget.repaintIfNeeded()
        }
      }
    }
  }

  private def updateProgressArea(): Unit = {
    if (started == 0) {
      progressArea.setText(I18N.gui("init"))
    } else {
      def pad(num: Long): String = {
        if (num < 10) {
          "0" + num
        } else {
          num.toString
        }
      }

      val duration = (System.currentTimeMillis - started).millis
      val formatted = s"${pad(duration.toHours)}:${pad(duration.toMinutes % 60)}:${pad(duration.toSeconds % 60)}"

      progressArea.setText(I18N.gui("progressArea", runCount.toString, totalRuns.toString, steps.toString, formatted,
                                    settingsString))
    }
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    progressArea.syncTheme()

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
    scrollPane.setBackground(InterfaceColors.textAreaBackground())

    updateViewCheckbox.setForeground(InterfaceColors.dialogText())
    plotsAndMonitorsSwitch.setForeground(InterfaceColors.dialogText())

    speedSlider.syncTheme()

    pauseButton.syncTheme()
    abortButton.syncTheme()

    plotWidgetOption.foreach(_.syncTheme())
  }
}
