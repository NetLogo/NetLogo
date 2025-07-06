// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.api.PreviewCommands
import org.nlogo.core.{ I18N, Model }
import org.nlogo.swing.UserAction._
import org.nlogo.window.{ GraphicsPreviewInterface, PreviewCommandsEditorInterface }
import org.nlogo.workspace.{ AbstractWorkspaceScala, WorkspaceFactory }

object PreviewCommandsEditor {
  val title = I18N.gui.get("tools.previewCommands.title")
  class EditPreviewCommands(
    previewCommandsEditor: => PreviewCommandsEditorInterface,
    workspace:             AbstractWorkspaceScala,
    f:                     () => Model) extends AbstractAction(title)
    with MenuAction {
      category    = ToolsCategory
      group       = ToolsDialogsGroup
      accelerator = KeyBindings.keystroke('P', withMenu = true, withShift = true)

      override def actionPerformed(actionEvent: ActionEvent): Unit = {
        val model = f()

        workspace.setPreviewCommands(previewCommandsEditor.getPreviewCommands(model, workspace.getModelPath))
      }
    }
}

class PreviewCommandsEditor(
  owner: Frame,
  workspaceFactory: WorkspaceFactory,
  graphicsPreview: GraphicsPreviewInterface)
  extends PreviewCommandsEditorInterface {
  val title = PreviewCommandsEditor.title
  def getPreviewCommands(model: Model, modelPath: String): PreviewCommands = {
    val dialog = new PreviewCommandsDialog(
      owner, title, model, modelPath,
      workspaceFactory, graphicsPreview)
    dialog.setVisible(true)
    val previewCommands = dialog.previewCommands
    previewCommands
  }
}
