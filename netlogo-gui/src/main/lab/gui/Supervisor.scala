// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import collection.mutable.{ HashMap, ListBuffer }

import java.awt.{ Dialog }
import java.io.{ FileWriter, IOException, PrintWriter }

import org.nlogo.api.{ Exceptions, LabProtocol, LogoException, PlotCompilationErrorAction }
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.lab.{ Exporter, PartialData, PostProcessor, SpreadsheetExporter, StatsProcessor, TableExporter, Worker }
import org.nlogo.nvm.{ EngineException, Workspace }
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.swing.{ OptionDialog }
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace }
import org.nlogo.workspace.{ CurrentModelOpener, WorkspaceFactory }
import scala.collection.mutable.Set

class Supervisor(
  dialog: Dialog,
  val workspace: GUIWorkspace,
  protocol: LabProtocol,
  factory: WorkspaceFactory with CurrentModelOpener,
  dialogFactory: EditDialogFactoryInterface,
  saveProtocol: (LabProtocol) => Unit,
) extends Thread("BehaviorSpace Supervisor") {
  var options = protocol.runOptions
  val worker = new Worker(protocol)
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
            System.err.println("Run #" + runNumber + ", RUNTIME ERROR: " + msg)
            ee.printStackTrace(System.err)
          case le: LogoException =>
            System.err.println("Run #" + runNumber + ", RUNTIME ERROR: " + le.getMessage)
            le.printStackTrace(System.err)
          case _ =>
            System.err.println("Run #" + runNumber + ", JAVA EXCEPTION: " + e.getMessage)
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
  private val progressDialog = new ProgressDialog(dialog, this, saveProtocol)
  private val exporters = new ListBuffer[Exporter]
  private val exporterFileNames = new HashMap[Exporter, String]
  private var spreadsheetExporter: SpreadsheetExporter = null
  private var tableExporter: TableExporter = null
  private val postProcessors = new ListBuffer[PostProcessor]
  worker.addListener(progressDialog)
  def addExporter(exporter: Exporter) {
    if (!exporters.contains(exporter)) {
      exporters += exporter
      worker.addListener(exporter)
    }
  }

  def addPostProcessor(postProcessor: PostProcessor) {
    postProcessors += postProcessor
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
        try {
          new RunOptionsDialog(dialog, dialogFactory, workspace.guessExportName(worker.protocol.name)).get
        }
        catch { case ex: UserCancelException => return }
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
            data = data.tail
            while (data != Nil) {
              partialData.data = partialData.data :+ data.head
              data = data.tail
            }
          }
          spreadsheetExporter = new SpreadsheetExporter(
            workspace.getModelFileName,
            workspace.world.getDimensions,
            worker.protocol,
            new PrintWriter(new FileWriter(fileName)),
            partialData)
          exporterFileNames(spreadsheetExporter) = fileName
          addExporter(spreadsheetExporter)
        } catch {
          case _: Throwable =>
            OptionDialog.showMessage(
              workspace.getFrame, "Error During Experiment",
              "Unable to read existing spreadsheet output, data is not intact.",
              Array(I18N.gui.get("common.buttons.ok")))
            return
        }
      }
      else {
        OptionDialog.showMessage(
          workspace.getFrame, "Error During Experiment",
          "Spreadsheet output file has been moved or deleted.",
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
        exporterFileNames(tableExporter) = fileName
        addExporter(tableExporter)
      }
      else {
        OptionDialog.showMessage(
          workspace.getFrame, "Error During Experiment",
          "Table output file has been moved or deleted.",
          Array(I18N.gui.get("common.buttons.ok")))
        return
      }
    }
    if (options.stats != null && options.stats.trim() != "") {
      if (tableExporter != null || spreadsheetExporter != null) {
        val fileName = options.stats.trim()
        try {
          addPostProcessor(new StatsProcessor(
            workspace.getModelFileName,
            workspace.world.getDimensions,
            worker.protocol,
            tableExporter,
            spreadsheetExporter,
            exporterFileNames,
            new PrintWriter(new FileWriter(fileName))))
        } catch {
          case e: IOException =>
            failure(e)
            return
        }
      } else {
        // EventQueue.invokeLater(new Runnable() {
        //   def run() {
        //     OptionDialog.showMessage(
        //       workspace.getFrame, "Warning: Statistics Exporter",
        //       "A table or spreadsheet must be enabled to use the statistics exporter",
        //       Array(I18N.gui.get("common.buttons.continue"))
        //     )
        //   }
        // })
        EventQueue.mustBeEventDispatchThread()
        OptionDialog.showMessage(
          workspace.getFrame, "Warning: Statistics Exporter",
          "A table or spreadsheet must be enabled to use the statistics exporter",
          Array(I18N.gui.get("common.buttons.continue"))
        )
      }
    }
    progressDialog.setUpdateView(options.updateView)
    progressDialog.setPlotsAndMonitorsSwitch(options.updatePlotsAndMonitors)
    queue.enqueue(workspace)
    (2 to options.threadCount).foreach{num =>
      val w = factory.newInstance
      // We want to print any plot compilation errors for just one of
      // the headless workspaces.
      if (num == 2) {
        w.setPlotCompilationErrorAction(PlotCompilationErrorAction.Output)
      } else {
        w.setPlotCompilationErrorAction(PlotCompilationErrorAction.Ignore)
      }
      factory.openCurrentModelIn(w)
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
      EventQueue.invokeLater(new Runnable() { def run() {
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

  def pause() {
    paused = true
  }

  def abort() {
    aborted = true
    interrupt()
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
        if (paused)
          progressDialog.saveProtocolP()
        else
          progressDialog.resetProtocol()
        progressDialog.close()
      } } )
    headlessWorkspaces.foreach(_.dispose())
    postProcessors.foreach(_.process())
  }

  private def failure(t: Throwable) {
    EventQueue.mustBeEventDispatchThread()
    t match {
      case ex: CompilerException =>
        OptionDialog.showMessage(
          workspace.getFrame, "Error During Experiment",
          "Experiment aborted due to syntax error:\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok"))
        )
      case ex: LogoException =>
        OptionDialog.showMessage(
          workspace.getFrame, "Error During Experiment",
          "Experiment aborted due to runtime error:\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok"))
        )
      case ex: IOException =>
        OptionDialog.showMessage(
          workspace.getFrame, "Error During Experiment",
          "Experiment aborted due to file input or output (I/O) error:\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok"))
        )
      case _ => Exceptions.handle(t)
    }
  }
}
