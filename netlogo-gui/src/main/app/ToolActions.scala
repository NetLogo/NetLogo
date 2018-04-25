// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Frame
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, JDialog }

import org.nlogo.api.AggregateManagerInterface
import org.nlogo.app.tools.{ ExtensionInfo, ExtensionStatus, ExtensionsAndLibrariesDialog }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.window.{ ColorDialog, LinkRoot }
import org.nlogo.shape.ShapesManagerInterface
import org.nlogo.swing.UserAction._

abstract class ShowDialogAction(name: String) extends AbstractAction(name) {
  def createDialog(): JDialog

  lazy protected val createdDialog = createDialog

  override def actionPerformed(e: ActionEvent): Unit = {
    createdDialog.setVisible(true)
  }
}

class ShowPreferencesDialog(newDialog: => JDialog)
extends ShowDialogAction(I18N.gui.get("menu.tools.preferences"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsSettingsGroup

  override def createDialog = newDialog
}

class OpenExtensionsAndLibrariesDialog(frame: Frame)
extends ShowDialogAction(I18N.gui.get("menu.tools.extensionsAndLibraries"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsSettingsGroup

  def createDialog() = new ExtensionsAndLibrariesDialog(frame, Array[ExtensionInfo](ExtensionInfo("Generic Extension", "Short desc", "this is a long description. more than one line", null, null, ExtensionStatus.UpToDate)))
}

class OpenColorDialog(frame: Frame) extends ShowDialogAction(I18N.gui.get("menu.tools.colorSwatches"))
  with MenuAction {
  category = ToolsCategory
  group = ToolsDialogsGroup

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
  extends AbstractAction(I18N.gui.get(s"menu.tools.$key"))
  with MenuAction {
  category = ToolsCategory
  group    = ToolsDialogsGroup

  override def actionPerformed(e: ActionEvent) {
    shapeManager.init(I18N.gui.get(s"menu.tools.$key"))
  }
}

class ShowSystemDynamicsModeler(aggregateManager: AggregateManagerInterface)
  extends AbstractAction(I18N.gui.get("menu.tools.systemDynamicsModeler"))
  with MenuAction {
  category    = ToolsCategory
  group       = ToolsDialogsGroup
  accelerator = KeyBindings.keystroke('D', withMenu = true, withShift = true)

  override def actionPerformed(e: ActionEvent) {
    aggregateManager.showEditor()
  }
}

class OpenHubNetClientEditor(workspace: AbstractWorkspaceScala, linkRoot: LinkRoot)
  extends AbstractAction(I18N.gui.get("menu.tools.hubNetClientEditor"))
  with MenuAction {
    category = ToolsCategory
    group    = ToolsHubNetGroup

  override def actionPerformed(e: ActionEvent) {
    workspace.getHubNetManager.foreach { mgr =>
      mgr.openClientEditor()
      linkRoot.addLinkComponent(mgr.clientEditor)
    }
  }
}
