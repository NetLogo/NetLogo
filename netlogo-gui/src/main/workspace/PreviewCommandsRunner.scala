package org.nlogo.workspace

import org.nlogo.core.{ AgentKind, CompilerException, Model }
import org.nlogo.api.{ FileIO, PreviewCommands, SimpleJobOwner }
import org.nlogo.nvm.Procedure
import java.awt.image.BufferedImage

import scala.util.Try

object PreviewCommandsRunner {

  class NonCompilableCommandsException
    extends IllegalStateException("Preview commands must be compilable")

  def fromFactory(
    workspaceFactory: WorkspaceFactory with CurrentModelOpener): PreviewCommandsRunner = {
    this(workspaceFactory, workspaceFactory.openCurrentModelIn)
  }

  def fromModelContents(
    workspaceFactory: WorkspaceFactory,
    model: Model,
    modelPath: String,
    previewCommands: PreviewCommands): PreviewCommandsRunner = {
    val open = (w: AbstractWorkspace) => {
      w.openModel(model)
      Option(modelPath).foreach(w.setModelPath)
    }
    this(workspaceFactory, open, Some(previewCommands))
  }

  def fromModelPath(
    workspaceFactory: WorkspaceFactory,
    modelPath: String): PreviewCommandsRunner = {
    this(workspaceFactory, _.open(modelPath))
  }

  def initWorkspace(
    workspaceFactory: WorkspaceFactory,
    openModelIn: AbstractWorkspace => Unit,
    previewCommands: Option[PreviewCommands] = None): AbstractWorkspace = {

    def newWorkspace(openModelIn: AbstractWorkspace => Unit) = {
      val ws = workspaceFactory.newInstance
      try {
        // set the seed before opening the model so it affects the `startup` procedure
        val jobOwner = new SimpleJobOwner(this.getClass.getName, ws.world.mainRNG, AgentKind.Observer)
        ws.evaluateCommands(jobOwner, "random-seed 0")
        openModelIn(ws)
        ws
      } catch {
        case e: Throwable =>
          // rethrow any exception, but at least try to dispose of the workspace
          ws.dispose(); throw e
      }
    }

    val ws = try newWorkspace(openModelIn) catch {
      case e: CompilerException =>
        // If we get a compiler exception when trying to open the model, we
        // open an empty workspace instead. This way, we get an exception
        // when trying to compile the preview commands themselves (most likely
        // something like "Nothing named SETUP has been defined") instead of
        // getting the error from the main code. This is consistent with the
        // way widgets behave when there is an error in the code tab.
        // And in the unlikely case where the preview commands don't depend on
        // the rest of the model, they'll work. NP 2016-01-12
        newWorkspace(_.openString(FileIO.getResourceAsString("/system/empty.nlogo")))
    }
    previewCommands.foreach(ws.previewCommands = _)
    ws
  }

  def apply(
    workspaceFactory: WorkspaceFactory,
    openModelIn: AbstractWorkspace => Unit,
    previewCommands: Option[PreviewCommands] = None): PreviewCommandsRunner = {

    val ws = initWorkspace(workspaceFactory, openModelIn, previewCommands)

    try {
      ws.previewCommands match {
        case compilableCommands: PreviewCommands.Compilable =>
          val procedure = ws.compileCommands(compilableCommands.source)
          new PreviewCommandsRunner(ws, procedure)
        case _ => // non-compilable preview commands
          ws.dispose()
          throw new NonCompilableCommandsException
      }
    } catch {
      case e: CompilerException =>
        ws.dispose()
        throw e
    }
  }

}

class PreviewCommandsRunner private (
  workspace: AbstractWorkspace,
  procedure: Procedure) {

  lazy val previewImage: Try[BufferedImage] = Try {
    try {
      val jobOwner = new SimpleJobOwner(this.getClass.getName, workspace.world.mainRNG, AgentKind.Observer)
      try
      workspace.evaluateCommands(jobOwner, "startup", workspace.world.observers, true)
      catch {
        case e: CompilerException => /* ignore */
      }
      workspace.runCompiledCommands(jobOwner, procedure)
      workspace.exportView
    } finally {
      workspace.dispose()
    }
  }

  trait Runnable extends java.lang.Runnable {
    def result: Option[Try[BufferedImage]]
  }

  lazy val runnable: Runnable = new Runnable {
    private var _result: Option[Try[BufferedImage]] = None
    override def result = _result
    override def run(): Unit = { _result = Some(previewImage) }
  }
}
