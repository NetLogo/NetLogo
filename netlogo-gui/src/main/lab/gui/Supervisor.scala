// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import collection.mutable.ListBuffer

import java.awt.Window
import java.io.{ File, FileNotFoundException, FileWriter, IOException, PrintWriter }

import org.nlogo.api.{ CompilerServices, Exceptions, ExportPlotWarningAction, LabProtocol, LogoException,
                       PlotCompilationErrorAction, LabPostProcessorInputFormat }
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.editor.Colorizer
import org.nlogo.lab.{ Exporter, ListsExporter, PartialData, SpreadsheetExporter, StatsExporter, TableExporter, Worker }
import org.nlogo.nvm.{ EngineException, Workspace }
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ EditDialogFactory, GUIWorkspace }
import org.nlogo.workspace.{ AbstractWorkspace, WorkspaceFactory }

import scala.collection.mutable.Set

class Supervisor(
  parent: Window,
  val workspace: AbstractWorkspace,
  protocol: LabProtocol,
  factory: WorkspaceFactory,
  dialogFactory: EditDialogFactory,
  compiler: CompilerServices,
  colorizer: Colorizer,
  saveProtocol: (LabProtocol, Int) => Unit,
  automated: Boolean
) extends Thread("BehaviorSpace Supervisor") {
  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.behaviorSpace")
  val worker = new Worker(protocol, writing)
  val headlessWorkspaces = new ListBuffer[Workspace]
  val queue = new collection.mutable.Queue[Workspace]
  val completed = Set[Int]()
  var highestCompleted = protocol.runsCompleted
  val listener =
    new ProgressListener {
      override def runCompleted(w: Workspace, runNumber: Int, step: Int): Unit = {
        completed += runNumber
        while (completed.contains(highestCompleted + 1))
          highestCompleted += 1
        queue.synchronized { if (!paused) queue.enqueue(w) }
      }
      override def runtimeError(w: Workspace, runNumber: Int, e: Throwable): Unit = {
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
  val runnable = new Runnable { override def run(): Unit = {
    worker.run(workspace, () => nextWorkspace, protocol.threadCount)
  } }
  private val workerThread = new Thread(runnable, "BehaviorSpace Worker")
  private val progressDialog = new ProgressDialog(parent, this, compiler, colorizer, saveProtocol)
  private val exporters = new ListBuffer[Exporter]
  private var spreadsheetExporter: SpreadsheetExporter = null
  private var spreadsheetFileName: String = null
  private var tableExporter: TableExporter = null
  private var tableFileName: String = null
  private var statsExporter: StatsExporter = null
  worker.addListener(progressDialog)
  def addExporter(exporter: Exporter): Unit = {
    if (!exporters.contains(exporter)) {
      exporters += exporter
      worker.addListener(exporter)
    }
  }

  override def start(): Unit = {
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

    if (protocol.runsCompleted == 0 && !automated) {
      try {
        new RunOptionsDialog(parent, dialogFactory, Option(workspace.getModelDir).map(new File(_).toPath),
                             workspace.guessExportName(protocol.name), protocol).run()
      } catch {
        case ex: UserCancelException => return
      }
    }

    if (protocol.spreadsheet != null && protocol.spreadsheet.trim() != "") {
      val fileName = protocol.spreadsheet.trim()
      if (protocol.runsCompleted == 0 || new File(fileName).exists) {
        try {
          val partialData = new PartialData
          if (protocol.runsCompleted > 0) {
            var data = scala.io.Source.fromFile(fileName).getLines().drop(6).toList
            partialData.runNumbers = "," + data.head.split(",", 2)(1)
            data = data.tail
            while (!data.head.contains("[")) {
              partialData.variables = partialData.variables :+ "," + data.head.split(",", 2)(1)
              data = data.tail
            }
            if (data.head.contains("[reporter]")) {
              partialData.reporters = "," + data.head.split(",", 2)(1)
              data = data.tail
              partialData.finals =  "," + data.head.split(",", 2)(1)
              data = data.tail
              partialData.mins = "," + data.head.split(",", 2)(1)
              data = data.tail
              partialData.maxes = "," + data.head.split(",", 2)(1)
              data = data.tail
              partialData.means = "," + data.head.split(",", 2)(1)
              data = data.tail
            }
            partialData.steps = "," + data.head.split(",", 2)(1)
            data = data.tail.tail
            partialData.dataHeaders = "," + data.head.split(",", 2)(1)
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
          case e: FileNotFoundException => return guiError(I18N.gui("error.pause.alreadyOpen"))
          case _: Throwable => return guiError(I18N.gui("error.pause.invalidSpreadsheet"))
        }
      }
      else return guiError(I18N.gui("error.pause.spreadsheet"))
    }
    if (protocol.table != null && protocol.table.trim() != "") {
      val fileName = protocol.table.trim()
      if (protocol.runsCompleted == 0 || new File(fileName).exists) {
        try {
          tableExporter = new TableExporter(
            workspace.getModelFileName,
            workspace.world.getDimensions,
            worker.protocol,
            new PrintWriter(new FileWriter(fileName, protocol.runsCompleted > 0)))
          tableFileName = fileName
          addExporter(tableExporter)
        } catch {
          case e: FileNotFoundException => return guiError(I18N.gui("error.pause.alreadyOpen"))
        }
      }
      else return guiError(I18N.gui("error.pause.table"))
    }
    if (protocol.stats != null && protocol.stats.trim() != "") {
      if (tableFileName != null || spreadsheetFileName != null) {
        val fileName = protocol.stats.trim()
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
          case e: FileNotFoundException => return guiError(I18N.gui("error.pause.alreadyOpen"))
          case e: IOException =>
            failure(e)
            return
        }
      } else return guiError(I18N.gui("error.stats"))
    }
    if (protocol.lists != null && protocol.lists.trim() != "") {
      val fileName = protocol.lists.trim()
      try {
        addExporter(new ListsExporter(
          workspace.getModelFileName,
          workspace.world.getDimensions,
          worker.protocol,
          new PrintWriter(new FileWriter(fileName)),
          if (protocol.table != null && protocol.table.trim() != "") {
            LabPostProcessorInputFormat.Table(protocol.table.trim())
          } else if (protocol.spreadsheet != null && protocol.spreadsheet.trim() != "") {
            LabPostProcessorInputFormat.Spreadsheet(protocol.spreadsheet.trim())
          } else return guiError(I18N.gui("error.lists"))))
      } catch {
        case e: FileNotFoundException => return guiError(I18N.gui("error.pause.alreadyOpen"))
        case e: IOException =>
          failure(e)
          return
      }
    }
    progressDialog.setUpdateView(protocol.updateView)
    progressDialog.setPlotsAndMonitorsSwitch(protocol.updatePlotsAndMonitors)
    progressDialog.enablePlotsAndMonitorsSwitch(protocol.updatePlotsAndMonitors)
    workspace.setShouldUpdatePlots(protocol.updatePlotsAndMonitors)
    workspace.setExportPlotWarningAction(ExportPlotWarningAction.Warn)
    workspace.setTriedToExportPlot(false)
    queue.enqueue(workspace)
    (2 to protocol.threadCount.min(protocol.countRuns)).foreach{num =>
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
      w.setShouldUpdatePlots(protocol.updatePlotsAndMonitors)
      headlessWorkspaces += w
      queue.enqueue(w)
    }
    // add this listener last, so it runs last, so the workspace doesn't get re-enqueued
    // too soon - ST 8/18/09
    worker.addListener(listener)
    super.start()
  }

  override def run(): Unit = {
    try {
      workerThread.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler {
          def uncaughtException(t: Thread, e: Throwable): Unit = {
            EventQueue.invokeLater(new Runnable() { def run(): Unit = { failure(e) } })
          }
        }
      )
      workerThread.start()
      EventQueue.invokeLater(new Runnable() { def run(): Unit = {
        progressDialog.setVisible(true)
      }})
      workerThread.join()
    }
    catch {
      case e: InterruptedException => // ignore
      case e: Throwable            => Exceptions.handle(e)
    }
    finally { bailOut() }
  }

  def pause(): Unit = {
    paused = true
  }

  def abort(): Unit = {
    aborted = true
    interrupt()
  }

  def writing(): Unit = {
    progressDialog.writing()
  }

  private def bailOut(): Unit = {
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
      new Runnable { def run(): Unit = {
        workspace.jobManager.haltPrimary()
        workspace.jobManager.haltSecondary()
        workspace.behaviorSpaceRunNumber(0)
        workspace.behaviorSpaceExperimentName("")
        workspace.setShouldUpdatePlots(true)
        if (paused)
          progressDialog.saveProtocolP()
        else
          progressDialog.resetProtocol()
        progressDialog.close()
      } } )
    headlessWorkspaces.foreach { w =>
      try {
        w.dispose()
      } catch {
        // if this is caught, the JobManager was in the middle of doing something,
        // probably means the user Halted so it's fine to ignore this (Isaac B 7/13/25)
        case _: InterruptedException =>
      }
    }
  }

  private def failure(t: Throwable): Unit = {
    EventQueue.mustBeEventDispatchThread()
    t match {
      case ex: CompilerException => guiError(I18N.gui("error.compilation") + "\n" + ex.getMessage)
      case ex: LogoException => guiError(I18N.gui("error.runtime") + "\n" + ex.getMessage)
      case ex: IOException => guiError(I18N.gui("error.io") + "\n" + ex.getMessage)
      case _ => Exceptions.handle(t)
    }
  }

  private def guiError(message: String): Unit = {
    new OptionPane(workspace.asInstanceOf[GUIWorkspace].getFrame, I18N.gui("error.title"), message,
                   OptionPane.Options.Ok, OptionPane.Icons.Error)
  }
}
