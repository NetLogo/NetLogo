// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that each
 have their own menu bar and menus ev 8/25/05 */

import java.awt.event.ActionEvent
import java.util.prefs.Preferences
import javax.swing.{ AbstractAction, JCheckBoxMenuItem }

import org.nlogo.api.ModelSettings
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.editor.Actions
import org.nlogo.core.I18N

class EditMenu(app: App) extends org.nlogo.swing.Menu(I18N.gui.get("menu.edit"))
with AppEvents.SwitchedTabsEvent.Handler
with org.nlogo.window.Events.LoadModelEvent.Handler
with org.nlogo.window.Events.AboutToQuitEvent.Handler
{

  implicit val i18nName = I18N.Prefix("menu.edit")
  val prefs = Preferences.userNodeForPackage(getClass)
  val lineNumbersKey = "line_numbers"

  val snapAction = new AbstractAction(I18N.gui("snapToGrid")) {
    def actionPerformed(e: ActionEvent) = app.workspace.snapOn(!app.workspace.snapOn)
  }
  val lineNumbersAction = new AbstractAction(I18N.gui("showLineNumbers")) {
    def actionPerformed(e: ActionEvent) =
      app.tabs.lineNumbersVisible = !app.tabs.lineNumbersVisible
  }

  //TODO i18n - do we need to change the shortcut keys too?
  setMnemonic('E')
  addMenuItem('Z', org.nlogo.editor.UndoManager.undoAction)
  addMenuItem('Y', org.nlogo.editor.UndoManager.redoAction)
  addSeparator()
  addMenuItem(I18N.gui("cut"), 'X', Actions.CUT_ACTION )
  addMenuItem(I18N.gui("copy"), 'C', Actions.COPY_ACTION)
  addMenuItem(I18N.gui("paste"), 'V', Actions.PASTE_ACTION)
  addMenuItem(I18N.gui("delete"), (java.awt.event.KeyEvent.VK_DELETE).toChar, Actions.DELETE_ACTION, false)
  addSeparator()
  addMenuItem(I18N.gui("selectAll"), 'A', Actions.SELECT_ALL_ACTION)
  addSeparator()
  addMenuItem(I18N.gui("find"), 'F', org.nlogo.app.common.FindDialog.FIND_ACTION)
  addMenuItem(I18N.gui("findNext"), 'G', org.nlogo.app.common.FindDialog.FIND_NEXT_ACTION)
  addSeparator()

  //TODO: Move this out of the menu
  val lineNumberPreference = prefs.get(lineNumbersKey, "false").toBoolean
  app.tabs.lineNumbersVisible = lineNumberPreference

  private val lineNumbersItem = addCheckBoxMenuItem(I18N.gui("showLineNumbers"), lineNumberPreference, lineNumbersAction)
  addSeparator()
  addMenuItem(I18N.gui("shiftLeft"), '[', org.nlogo.editor.Actions.shiftLeftAction)
  addMenuItem(I18N.gui("shiftRight"), ']', org.nlogo.editor.Actions.shiftRightAction)
  addMenuItem(I18N.gui("format"), (java.awt.event.KeyEvent.VK_TAB).toChar, org.nlogo.editor.Actions.tabKeyAction, false)
  addSeparator()
  addMenuItem(I18N.gui("comment") + " / " + I18N.gui("uncomment"), ';', org.nlogo.editor.Actions.commentToggleAction)
  addSeparator()
  private val snapper = addCheckBoxMenuItem(I18N.gui("snapToGrid"), app.workspace.snapOn, snapAction)

  lineNumbersAction.setEnabled(false)
  if (lineNumbersItem.isSelected) lineNumbersAction.actionPerformed(null)

  addMenuListener(new javax.swing.event.MenuListener() {
    override def menuSelected(e: javax.swing.event.MenuEvent): Unit = {
      Actions.CUT_ACTION.setEnabled(app.tabs.codeTab.isTextSelected())
      Actions.COPY_ACTION.setEnabled(app.tabs.codeTab.isTextSelected())
      Actions.PASTE_ACTION.setEnabled(java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
        .isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor))
    }

    override def menuDeselected(e: javax.swing.event.MenuEvent): Unit = {
    }

    override def menuCanceled(e: javax.swing.event.MenuEvent): Unit = {
    }
  })

  final def handle(e: AppEvents.SwitchedTabsEvent) {
    snapAction.setEnabled(e.newTab == app.tabs.interfaceTab)
    lineNumbersAction.setEnabled(e.newTab != app.tabs.interfaceTab && e.newTab != app.tabs.infoTab)
  }

  def handle(e: org.nlogo.window.Events.LoadModelEvent) {
    e.model.optionalSectionValue[ModelSettings]("org.nlogo.modelsection.modelsettings") match {
      case Some(settings: ModelSettings) =>
        app.workspace.snapOn(settings.snapToGrid)
        snapper.setState(settings.snapToGrid)
      case _ =>
    }
  }

  def handle(e: org.nlogo.window.Events.AboutToQuitEvent) =
    prefs.put(lineNumbersKey, lineNumbersItem.isSelected.toString)
}
