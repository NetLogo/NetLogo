package org.nlogo.app.previewcommands

import java.awt.Frame

import org.nlogo.api.PreviewCommands
import org.nlogo.window.GraphicsPreviewInterface
import org.nlogo.window.PreviewCommandsEditorInterface
import org.nlogo.workspace.WorkspaceFactory

class PreviewCommandsEditor(
  owner: Frame,
  workspaceFactory: WorkspaceFactory,
  graphicsPreview: GraphicsPreviewInterface)
  extends PreviewCommandsEditorInterface {
  val title = "Preview Commands Editor"
  def getPreviewCommands(modelContent: String, modelPath: String): PreviewCommands = {
    val dialog = new PreviewCommandsDialog(
      owner, title, modelContent, modelPath,
      workspaceFactory, graphicsPreview)
    dialog.setVisible(true)
    val previewCommands = dialog.previewCommands
    previewCommands
  }
}
