package org.nlogo.app.tools

import java.awt.Frame

import org.nlogo.api.PreviewCommands
import org.nlogo.core.Model
import org.nlogo.window.{ GraphicsPreviewInterface, PreviewCommandsEditorInterface }
import org.nlogo.workspace.WorkspaceFactory

class PreviewCommandsEditor(
  owner: Frame,
  workspaceFactory: WorkspaceFactory,
  graphicsPreview: GraphicsPreviewInterface)
  extends PreviewCommandsEditorInterface {
  val title = "Preview Commands Editor"
  def getPreviewCommands(model: Model, modelPath: String): PreviewCommands = {
    val dialog = new PreviewCommandsDialog(
      owner, title, model, modelPath,
      workspaceFactory, graphicsPreview)
    dialog.setVisible(true)
    val previewCommands = dialog.previewCommands
    previewCommands
  }
}
