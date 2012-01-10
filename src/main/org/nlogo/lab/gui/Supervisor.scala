// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.awt.UserCancelException
import org.nlogo.lab.{Exporter,Protocol,SpreadsheetExporter,TableExporter,Worker}
import org.nlogo.window.{EditDialogFactoryInterface,GUIWorkspace}
import org.nlogo.nvm.{EngineException, Workspace, WorkspaceFactory}
import org.nlogo.nvm.LabInterface.ProgressListener
import org.nlogo.api.{I18N, CompilerException, LogoException}

object Supervisor {
  case class RunOptions(threadCount: Int, table: Boolean, spreadsheet: Boolean)
}
class Supervisor(dialog: java.awt.Dialog,
                 val workspace: GUIWorkspace,
                 protocol: Protocol,
                 factory: WorkspaceFactory,
                 dialogFactory: EditDialogFactoryInterface)
  extends Thread("BehaviorSpace Supervisor")
{
  var options:Supervisor.RunOptions = null
  val worker = new Worker(protocol)
  val headlessWorkspaces = new collection.mutable.ListBuffer[Workspace]
  val queue = new collection.mutable.Queue[Workspace]
  val listener =
    new ProgressListener {
      override def runCompleted(w: Workspace, runNumber: Int, step: Int) {
        queue.synchronized { queue.enqueue(w) } }
      override def runtimeError(w: Workspace, runNumber: Int, e: Throwable) {
        e match {
          case ee: EngineException =>
            val msg = ee.context.buildRuntimeErrorMessage(ee.instruction,ee)
            System.err.println("Run #" + runNumber + ", RUNTIME ERROR: " + msg)
            ee.printStackTrace(System.err)
          case le: LogoException =>
            System.err.println("Run #" + runNumber + ", RUNTIME ERROR: " + le.getMessage)
            le.printStackTrace(System.err)
          case _ =>
            System.err.println("Run #" + runNumber + ", JAVA EXCEPTION: " + e.getMessage)
            e.printStackTrace(System.err)
        }
        org.nlogo.util.Exceptions.handle(e)
        queue.synchronized { queue.enqueue(w) }
      }}
  def nextWorkspace = queue.synchronized { queue.dequeue() }
  val runnable = new Runnable { override def run() {
    worker.run(workspace, nextWorkspace _, options.threadCount)
  } }
  private val workerThread  = new Thread(runnable, "BehaviorSpace Worker")
  private val progressDialog = new ProgressDialog(dialog,  this)
  private val exporters = new collection.mutable.ListBuffer[Exporter]
  worker.addListener(progressDialog)
  def addExporter(exporter: Exporter) {
    if(!exporters.contains(exporter)) {
      exporters += exporter
      worker.addListener(exporter)
    }
  }
  def hasSpreadsheetExporter = exporters.exists(_.isInstanceOf[SpreadsheetExporter])
  override def start() {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    workspace.jobManager.haltSecondary()
    workspace.jobManager.haltPrimary()
    try worker.compile(workspace) // result discarded. just to make sure compilation succeeds
    catch { case e: CompilerException => failure(e); return }
    options =
      try { new RunOptionsDialog(dialog, dialogFactory).get }
      catch{ case ex: UserCancelException => return }
    if(options.spreadsheet) {
      val fileName = org.nlogo.swing.FileDialog.show(
        workspace.getFrame, "Exporting as spreadsheet", java.awt.FileDialog.SAVE,
        workspace.guessExportName(worker.protocol.name + "-spreadsheet.csv"))
      addExporter(new SpreadsheetExporter(
        workspace.getModelFileName,
        workspace.world.getDimensions,
        worker.protocol,
        new java.io.PrintWriter(new java.io.FileWriter(fileName))))
    }
    if(options.table) {
      val fileName = org.nlogo.swing.FileDialog.show(
        workspace.getFrame, "Exporting as table", java.awt.FileDialog.SAVE,
        workspace.guessExportName(worker.protocol.name + "-table.csv"))
      addExporter(new TableExporter(
        workspace.getModelFileName,
        workspace.world.getDimensions,
        worker.protocol,
        new java.io.PrintWriter(new java.io.FileWriter(fileName))))
    }
    queue.enqueue(workspace)
    (2 to options.threadCount).foreach{_ =>
      val w = factory.newInstance
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
            org.nlogo.awt.EventQueue.invokeLater(new Runnable() { def run() { failure(e) } } )
          }})
      workerThread.start()
      org.nlogo.awt.EventQueue.invokeLater(new Runnable() { def run() {
        progressDialog.setVisible(true) } })
      workerThread.join()
      org.nlogo.awt.EventQueue.invokeLater(new Runnable { def run() {
        progressDialog.close() } })
    }
    catch {
      case e: InterruptedException => // ignore
      case e => org.nlogo.util.Exceptions.handle(e)
    }
    finally { bailOut() }
  }
  private def bailOut() {
    worker.abort()
    workspace.jobManager.haltPrimary()
    workerThread.interrupt()
    while(workerThread.isAlive) Thread.sleep(100)
    org.nlogo.awt.EventQueue.invokeLater(
      new Runnable { def run() {
        workspace.jobManager.haltPrimary()
        workspace.jobManager.haltSecondary()
        workspace.behaviorSpaceRunNumber(0)
        progressDialog.close()
      } } )
    headlessWorkspaces.foreach(_.dispose())
  }
  private def failure(t: Throwable) {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    t match {
      case ex: CompilerException =>
        org.nlogo.swing.OptionDialog.show(
          workspace.getFrame, "Error During Experiment",
          "Experiment aborted due to syntax error:\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok")))
      case ex: LogoException =>
        org.nlogo.swing.OptionDialog.show(
          workspace.getFrame, "Error During Experiment",
          "Experiment aborted due to runtime error:\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok")))
      case ex: java.io.IOException =>
        org.nlogo.swing.OptionDialog.show(
          workspace.getFrame, "Error During Experiment",
          "Experiment aborted due to I/O error:\n" + ex.getMessage,
          Array(I18N.gui.get("common.buttons.ok")))
      case _ => org.nlogo.util.Exceptions.handle(t)
    }
  }
}
