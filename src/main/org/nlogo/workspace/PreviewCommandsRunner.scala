package org.nlogo.workspace

import java.awt.image.BufferedImage

import org.nlogo.api.CompilerException
import org.nlogo.api.JobOwner
import org.nlogo.api.Observer
import org.nlogo.api.PreviewCommands
import org.nlogo.api.SimpleJobOwner
import org.nlogo.nvm.Workspace
import org.nlogo.nvm.Procedure

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
    modelContents: String,
    previewCommands: PreviewCommands): PreviewCommandsRunner = {
    this(workspaceFactory, _.openString(modelContents), Some(previewCommands))
  }

  def fromModelPath(
    workspaceFactory: WorkspaceFactory,
    modelPath: String): PreviewCommandsRunner = {
    this(workspaceFactory, _.open(modelPath))
  }

  def apply(
    workspaceFactory: WorkspaceFactory,
    openModelIn: Workspace => Unit,
    previewCommands: Option[PreviewCommands] = None): PreviewCommandsRunner = {

    val ws = workspaceFactory.newInstance
    val jobOwner = new SimpleJobOwner(this.getClass.getName, ws.world.mainRNG, classOf[Observer])

    // set the seed before opening the model so it affects the `startup` procedure
    ws.evaluateCommands(jobOwner, "random-seed 0")
    openModelIn(ws)
    previewCommands.foreach(ws.previewCommands = _)
    ws.previewCommands match {
      case compilableCommands: PreviewCommands.Compilable =>
        val procedure = ws.compileCommands(compilableCommands.source)
        new PreviewCommandsRunner(ws, procedure, jobOwner)
      case _ => // non-compilable preview commands
        ws.dispose()
        throw new NonCompilableCommandsException
    }
  }

}

class PreviewCommandsRunner private (
  workspace: Workspace,
  procedure: Procedure,
  jobOwner: JobOwner) {

  lazy val previewImage: Try[BufferedImage] = Try {
    try
      workspace.evaluateCommands(jobOwner, "startup", workspace.world.observers, true)
    catch {
      case e: CompilerException => /* ignore */
    }
    workspace.runCompiledCommands(jobOwner, procedure)
    val image = workspace.exportView
    workspace.dispose()
    image
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
