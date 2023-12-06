// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import collection.mutable.ListBuffer

import java.awt.{ Dialog }
import java.io.{ FileWriter, IOException, PrintWriter }

import org.nlogo.api.{ Exceptions, ExportPlotWarningAction, LabProtocol,
  LogoException, PlotCompilationErrorAction, LabPostProcessorInputFormat }
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.lab.{ Exporter, ListsExporter, PartialData, SpreadsheetExporter, StatsExporter, TableExporter, Worker }
import org.nlogo.nvm.{ EngineException, Workspace }
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.swing.{ OptionDialog }
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace }
import org.nlogo.workspace.{ CurrentModelOpener, WorkspaceFactory }
import scala.collection.mutable.Set

object Supervisor {
  def runFromExtension(protocol: LabProtocol, workspace: GUIWorkspace) {
    new Supervisor(null, workspace, protocol, null, null, (_) => {}, false).start()
  }
}

class Supervisor(
  dialog: Dialog,
  val workspace: GUIWorkspace,
  protocol: LabProtocol,
  factory: WorkspaceFactory with CurrentModelOpener,
  dialogFactory: EditDialogFactoryInterface,
  saveProtocol: (LabProtocol) => Unit,
  useGUI: Boolean = true
) extends Thread("BehaviorSpace Supervisor") {
  private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace")
  var options = protocol.runOptions
  val worker = new Worker(protocol, writing)
  val headlessWorkspaces = new ListBuffer[Workspace]
  val queue = new collection.mutable.Queue[Workspace]
  val completed = Set[Int]()
  var highestCompleted = protocol.runsCompleted
  val listener =
    new ProgressListener {
      override def runCompleted(w: Workspace, runNumber: Int, step: Int) {
        completed += runNumber
        while (completed.contains(highestCompleted + 1))
          highestCompleted += 1
        queue.synchronized { if (!paused) queue.enqueue(w) }
      }
      override def runtimeError(w: Workspace, runNumber: Int, e: Throwable) {
        e match {
          case ee: EngineException =>
            val msg = ee.runtimeErrorMessage
            System.err.println(I18N.gui("error.print.runNumber") +
              runNumber + ", " + I18N.gui("error.print.runtime") + ": " + msg)
            ee.printStackTrace(System.err)
          case le: LogoException =>
            System.err.println(I18N.gui("error.print.runNumber") +
              runNumber + ", " + I18N.gui("error.print.runtime") + ": " + le.getMessage)
            le.printStackTrace(System.err)
          case _ =>
            System.err.println(I18N.gui("error.print.runNumber") +
              runNumber + ", " + I18N.gui("error.print.javaException") + ": " + e.getMessage)
            e.printStackTrace(System.err)
        }
        Exceptions.handle(e)
      }}
  var paused = false
  var aborted = false

  def nextWorkspace = queue.synchronized { if (queue.isEmpty) null else queue.dequeue() }
  val runnable = new Runnable { override def run() {
    worker.run(workspace, nextWorkspace _, options.threadCount)
  } }
  private val workerThread = new Thread(runnable, "BehaviorSpace Worker")
  private val progressDialog =
    if (useGUI) new ProgressDialog(dialog, this, saveProtocol)
    else null
  private val exporters = new ListBuffer[Exporter]
  private var spreadsheetExporter: SpreadsheetExporter = null
  private var spreadsheetFileName: String = null
  private var tableExporter: TableExporter = null
  private var tableFileName: String = null
  private var statsExporter: StatsExporter = null
  if (useGUI) worker.addListener(progressDialog)
  def addExporter(exporter: Exporter) {
    if (!exporters.contains(exporter)) {
      exporters += exporter
      worker.addListener(exporter)
    }
  }

  override def start() {
    EventQueue.mustBeEventDispatchThread()
    workspace.jobManager.haltSecondary()
    workspace.jobManager.haltPrimary()
    try {
      worker.compile(workspace) // result discarded. just to make sure compilation succeeds
    } catch {
      case e: CompilerException =>
        failure(e)
        return
    }

    if (options == null) {
      options =
        if (useGUI) {
          try {
            new RunOptionsDialog(dialog, dialogFactory, workspace.guessExportName(worker.protocol.name)).get
          }
          catch { case ex: UserCancelException => return }
        } else {
          RunOptionsDialog.default
        }
    }

    if (options.spreadsheet != null && options.spreadsheet.trim() != "") {
      val fileName = options.spreadsheet.trim()
      if (protocol.runsCompleted == 0 || new java.io.File(fileName).exists) {
        try {
          val partialData = new PartialData
          if (protocol.runsCompleted > 0) {
            var data = scala.io.Source.fromFile(fileName).getLines().drop(6).toList
            partialData.runNumbers = ',' + data.head.split(",", 2)(1)
            data = data.tail
            while (!data.head.contains("[")) {
              partialData.variables = partialData.variables :+ ',' + data.head.split(",", 2)(1)
              data = data.tail
            }
            if (data.head.contains("[reporter]")) {
              partialData.reporters = ',' + data.head.split(",", 2)(1)
              data = data.tail
              partialData.finals =  ',' + data.head.split(",", 2)(1)
              data = data.tail
              partialData.mins = ',' + data.head.split(",", 2)(1)
              data = data.tail
              partialData.maxes = ',' + data.head.split(",", 2)(1)
              data = data.tail
              partialData.means = ',' + data.head.split(",", 2)(1)
              data = data.tail
            }
            partialData.steps = ',' + data.head.split(",", 2)(1)
            data = data.tail.tail
            partialData.dataHeaders = ',' + data.head.split(",", 2)(1)
            partialData.data = data.tail
          }
          spreadsheetExporter = new SpreadsheetExporter(
            workspace.getModelFileName,
            workspace.world.getDimensions,
            worker.protocol,
            new PrintWriter(new FileWriter(fileName)),
            partialData)
          spreadsheetFileName = fileName
          addExporter(spreadsheetExporter)
        } catch {
          case _: Throwable =>
            OptionDialog.showMessage(
              workspace.getFrame, I18N.gui("error.title"),
              I18N.gui("error.pause.invalidSpreadsheet"),
              Array(I18N.gui.get("common.buttons.ok")))
            return
        }
      }
      else {
        OptionDialog.showMessage(
          workspace.getFrame, I18N.gui("error.title"),
          I18N.gui("error.pause.spreadsheet"),
          Array(I18N.gui.get("common.buttons.ok")))
        return
      }
    }
    if (options.table != null && options.table.trim() != "") {
      val fileName = options.table.trim()
      if (protocol.runsCompleted == 0 || new java.io.File(fileName).exists) {
        tableExporter = new TableExporter(
          workspace.getModelFileName,
          workspace.world.getDimensions,
          worker.protocol,
          new PrintWriter(new FileWriter(fileName, protocol.runsCompleted > 0)))
        tableFileName = fileName
        addExporter(tableExporter)
      }
      else {
        OptionDialog.showMessage(
          workspace.getFrame, I18N.gui("error.title"),
          I18N.gui("error.pause.table"),
          Array(I18N.gui.get("common.buttons.ok")))
        return
      }
    }
    if (options.stats != null && options.stats.trim() != "") {
      if (tableFileName != null || spreadsheetFileName != null) {
        val fileName = options.stats.trim()
        try {
          statsExporter = new StatsExporter(
            workspace.getModelFileName,
            workspace.world.getDimensions,
            worker.protocol,
            new PrintWriter(new FileWriter(fileName)),
            {
              if (tableExporter != null) LabPostProcessorInputFormat.Table(tableFileName)
              else LabPostProcessorInputFormat.Spreadsheet(spreadsheetFileName)
            })
          addExporter(statsExporter)
        } catch {
          case e: IOException =>
            failure(e)
            return
        }
      } else {
        EventQueue.mustBeEventDispatchThread()
        OptionDialog.showMessage(
          workspace.getFrame, I18N.gui("error.title"),
          I18N.gui("error.stats"),
          Array(I18N.gui.get("common.buttons.ok"))
        )
        return
      }
    }
    if (options.lists != null && options.lists.trim() != "") {
      val fileName = options.lists.trim()
      try {
        addExporter(new ListsExporter(
          workspace.getModelFileName,
          workspace.world.getDimensions,
          worker.protocol,
          new PrintWriter(new FileWriter(fileName)),
          if (options.table != null && options.table.trim() != "") {
            LabPostProcessorInputFormat.Table(options.table.trim())
          } else if (options.spreadsheet != null && options.spreadsheet.trim() != "") {
            LabPostProcessorInputFormat.Spreadsheet(options.spreadsheet.trim())
          } else {
            OptionDialog.showMessage(
              workspace.getFrame, I18N.gui("error.title"),
              I18N.gui("error.lists"),
              Array(I18N.gui.get("common.buttons.ok")))
            return
          }))
      } catch {
        case e: IOException =>
          failure(e)
          return
      }
    }
    if (useGUI) {
      progressDialog.setUpdateView(options.updateView)
      progressDialog.setPlotsAndMonitorsSwitch(options.updatePlotsAndMonitors)
      progressDialog.enablePlotsAndMonitorsSwitch(options.updatePlotsAndMonitors)
    }
    workspace.setShouldUpdatePlots(options.updatePlotsAndMonitors)
    workspace.setExportPlotWarningAction(ExportPlotWarningAction.Warn)
    workspace.setTriedToExportPlot(false)
    queue.enqueue(workspace)
    (2 to options.threadCount).foreach{num =>
      val w = factory.newInstance
      // We want to print any plot compilation errors for just one of
      // the headless workspaces.
      if (num == 2) {
        w.setPlotCompilationErrorAction(PlotCompilationErrorAction.Output)
        w.setExportPlotWarningAction(ExportPlotWarningAction.Output)
      } else {
        w.setPlotCompilationErrorAction(PlotCompilationErrorAction.Ignore)
        w.setExportPlotWarningAction(ExportPlotWarningAction.Ignore)
      }
      factory.openCurrentModelIn(w)
      w.setShouldUpdatePlots(options.updatePlotsAndMonitors)
      headlessWorkspaces += w
      queue.enqueue(w)
    }
    // add this listener last, so it runs last, so the workspace doesn't get re-enqueued
    // too soon - ST 8/18/09
    worker.addListener(listener)
    super.start()
  }

  override def run() {
    try {
      workerThread.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler {
          def uncaughtException(t: Thread, e: Throwable) {
            EventQueue.invokeLater(new Runnable() { def run() { failure(e) } })
          }
        }
      )
      workerThread.start()
      if (useGUI) {
        EventQueue.invokeLater(new Runnable() { def run() {
          progressDialog.setVisible(true)
        }})
      }
      workerThread.join()
    }
    catch {
      case e: InterruptedException => // ignore
      case e: Throwable            => Exceptions.handle(e)
    }
    finally { bailOut() }
  }

  def pause() {
    paused = true
  }

  def abort() {
    aborted = true
    interrupt()
  }

  def writing() {
    if (useGUI) progressDialog.writing()
  }

  private def bailOut() {
    worker.abort()
    workspace.jobManager.haltPrimary()
    workerThread.interrupt()
    while (workerThread.isAlive) {
      try {
        Thread.sleep(100)
      } catch {
        case e: InterruptedException => // ignore
      }
    }
    EventQueue.invokeLater(
      new Runnable { def run() {
        workspace.jobManager.haltPrimary()
        workspace.jobManager.haltSecondary()
        workspace.behaviorSpaceRunNumber(0)
        workspace.behaviorSpaceExperimentName("")
        workspace.setShouldUpdatePlots(true)
        if (useGUI) {
          if (paused)
            progressDialog.saveProtocolP()
          else
            progressDialog.resetProtocol()
          progressDialog.close()
        }
      } } )
    headlessWorkspaces.foreach(_.dispose())
  }

  private def failure(t: Throwable) {
    EventQueue.mustBeEventDispatchThread()
    t match {
      case ex: CompilerException =>
        OptionDialog.showMessage(
          workspace.getFrame, I18N.gui("error.title"),
          I18N.gui("error.compilation") + "\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok"))
        )
      case ex: LogoException =>
        OptionDialog.showMessage(
          workspace.getFrame, I18N.gui("error.title"),
          I18N.gui("error.runtime") + "\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok"))
        )
      case ex: IOException =>
        OptionDialog.showMessage(
          workspace.getFrame, I18N.gui("error.title"),
          I18N.gui("error.io") + "\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok"))
        )
      case _ => Exceptions.handle(t)
    }
  }
}
