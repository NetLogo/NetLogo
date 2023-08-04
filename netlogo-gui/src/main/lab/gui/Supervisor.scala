// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import collection.mutable.ListBuffer

import java.awt.{ Dialog }
import java.io.{ FileWriter, IOException, PrintWriter }

import org.nlogo.api.{ Exceptions, LabProtocol, LogoException, PlotCompilationErrorAction }
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.lab.{ Exporter, SpreadsheetExporter, TableExporter, Worker }
import org.nlogo.nvm.{ EngineException, Workspace }
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.swing.{ OptionDialog }
import org.nlogo.window.{ EditDialogFactoryInterface, GUIWorkspace }
import org.nlogo.workspace.{ CurrentModelOpener, WorkspaceFactory }

import scala.collection.mutable.Set

object Supervisor {
  case class RunOptions(threadCount: Int, table: String, spreadsheet: String, updateView: Boolean, updatePlotsAndMonitors: Boolean)
}
class Supervisor(
  dialog: Dialog,
  val workspace: GUIWorkspace,
  protocol: LabProtocol,
  factory: WorkspaceFactory with CurrentModelOpener,
  dialogFactory: EditDialogFactoryInterface,
  progressDialog: ProgressDialog,
  previousOptions: Supervisor.RunOptions = null
) extends Thread("BehaviorSpace Supervisor") {
  var options: Supervisor.RunOptions = previousOptions
  val headlessWorkspaces = new ListBuffer[Workspace]
  val queue = new collection.mutable.Queue[Workspace]
  val completed = Set[Int]()
  var highestCompleted = 0 max protocol.runsCompleted
  val worker = new Worker(protocol)
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

  progressDialog.connectSupervisor(this)

  def nextWorkspace = queue.synchronized { if (queue.isEmpty) None else Some(queue.dequeue()) }
  val runnable = new Runnable { override def run() {
    worker.run(workspace, nextWorkspace _, options.threadCount)
  } }
  private val workerThread = new Thread(runnable, "BehaviorSpace Worker")
  private val exporters = new ListBuffer[Exporter]
  worker.addListener(progressDialog)
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
        try {
          new RunOptionsDialog(dialog, dialogFactory, workspace.guessExportName(worker.protocol.name)).get
        }
        catch { case ex: UserCancelException => return }
    }

    if (options.spreadsheet != null && options.spreadsheet.trim() != "") {
      val fileName = options.spreadsheet.trim()
      try {
        addExporter(new SpreadsheetExporter(
          workspace.getModelFileName,
          workspace.world.getDimensions,
          worker.protocol,
          new PrintWriter(new FileWriter(fileName, highestCompleted > 0))))
	    } catch {
		    case e: IOException =>
          failure(e)
          return
      }
    }
    if (options.table != null && options.table.trim() != "") {
      val fileName = options.table.trim()
      try {
        addExporter(new TableExporter(
          workspace.getModelFileName,
          workspace.world.getDimensions,
          worker.protocol,
          new PrintWriter(new FileWriter(fileName, highestCompleted > 0))))
	    } catch {
		    case e: IOException =>
          failure(e)
          return
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
        progressDialog.disconnectSupervisor()
        if (aborted) {
          progressDialog.promptSave()
        }
        else if (!paused) {
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
