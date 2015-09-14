package org.nlogo.nvm

import java.awt.image.BufferedImage

import scala.util.Try

import org.nlogo.api.PreviewCommands
import org.nlogo.api.SimpleJobOwner

class RunnablePreviewCommands(
  val previewCommands: PreviewCommands.Compilable,
  val modelContents: String,
  workspaceFactory: WorkspaceFactory)
  extends Runnable {
  private var _result: Option[Try[BufferedImage]] = None
  def result = _result
  override def run() = {
    val workspace = workspaceFactory.newInstance(false)
    val jobOwner = new SimpleJobOwner(this.getClass.getName, workspace.world.mainRNG)
    _result = Some(Try {
      workspace.evaluateCommands(jobOwner, "random-seed 0", true)
      workspace.openString(modelContents)
      workspace.evaluateCommands(jobOwner, previewCommands.source, true)
      workspace.exportView
    })
    workspace.dispose()
  }
}
