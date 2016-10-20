// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Frame, Toolkit }
import java.awt.event.{ ActionEvent, InputEvent }
import javax.swing.{ AbstractAction, Action, JDialog, KeyStroke }

import org.nlogo.api.AggregateManagerInterface
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.window.{ ColorDialog, LabManagerInterface, LinkRoot }
import org.nlogo.shape.ShapesManagerInterface
import org.nlogo.swing.UserAction._

abstract class ShowDialogAction(name: String) extends AbstractAction(name) {
  def createDialog(): JDialog

  lazy protected val createdDialog = createDialog

  override def actionPerformed(e: ActionEvent): Unit = {
    createdDialog.setVisible(true)
  }
}

class ShowPreferencesDialog(newDialog: => JDialog) extends ShowDialogAction(I18N.gui.get("menu.tools.preferences")) {
  putValue(ActionCategoryKey, ToolsCategory)
  putValue(ActionGroupKey,    "org.nlogo.app.Preferences")

  def createDialog(): JDialog = newDialog
}

class OpenColorDialog(frame: Frame) extends ShowDialogAction(I18N.gui.get("menu.tools.colorSwatches")) {
  putValue(ActionCategoryKey, ToolsCategory)
  putValue(ActionGroupKey,    ToolsDialogsGroup)

  def createDialog() = new ColorDialog(frame, false)

  var hasShown = false

  override def actionPerformed(e: ActionEvent) {
    Positioning.center(createdDialog, frame)
    if (!hasShown) {
      createdDialog.asInstanceOf[ColorDialog].showDialog()
      hasShown = true
    } else
      super.actionPerformed(e)
  }
}

class ShowShapeManager(key: String, shapeManager: => ShapesManagerInterface)
  extends AbstractAction(I18N.gui.get(s"menu.tools.$key")) {
  putValue(ActionCategoryKey, ToolsCategory)
  putValue(ActionGroupKey,    ToolsDialogsGroup)

  override def actionPerformed(e: ActionEvent) {
    shapeManager.init(I18N.gui.get(s"menu.tools.$key"))
  }
}

class ShowLabManager(labManager: LabManagerInterface) extends AbstractAction(I18N.gui.get(s"menu.tools.behaviorSpace")) {
  putValue(ActionCategoryKey, ToolsCategory)
  putValue(ActionGroupKey,    ToolsDialogsGroup)
  putValue(Action.ACCELERATOR_KEY, KeyBindings.keystroke('B', withMenu = true, withShift = true))

  override def actionPerformed(e: ActionEvent) {
    labManager.show()
  }
}

class ShowSystemDynamicsModeler(aggregateManager: AggregateManagerInterface)
  extends AbstractAction(I18N.gui.get("menu.tools.systemDynamicsModeler")) {
  putValue(ActionCategoryKey, ToolsCategory)
  putValue(ActionGroupKey,    ToolsDialogsGroup)
  putValue(Action.ACCELERATOR_KEY, KeyBindings.keystroke('D', withMenu = true, withShift = true))

  override def actionPerformed(e: ActionEvent) {
    aggregateManager.showEditor()
  }
}

class OpenHubNetClientEditor(workspace: AbstractWorkspaceScala, linkRoot: LinkRoot)
  extends AbstractAction(I18N.gui.get("menu.tools.hubNetClientEditor")) {
    putValue(ActionCategoryKey, ToolsCategory)
    putValue(ActionGroupKey,    ToolsHubNetGroup)

  override def actionPerformed(e: ActionEvent) {
    workspace.getHubNetManager.foreach { mgr =>
      mgr.openClientEditor()
      linkRoot.addLinkComponent(mgr.clientEditor)
    }
  }
}
