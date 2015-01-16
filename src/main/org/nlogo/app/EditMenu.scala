// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that each
 have their own menu bar and menus ev 8/25/05 */

import org.nlogo.editor.Actions
import org.nlogo.api.I18N
import org.nlogo.window.EditorColorizer

class EditMenu(app: App) extends org.nlogo.swing.Menu(I18N.gui.get("menu.edit"))
with Events.SwitchedTabsEventHandler
with org.nlogo.window.Events.LoadSectionEventHandler
{

  implicit val i18nName = I18N.Prefix("menu.edit")

  val snapAction = new javax.swing.AbstractAction(I18N.gui("snapToGrid")) {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      app.workspace.snapOn(!app.workspace.snapOn)
    }}

  private var snapper: javax.swing.JCheckBoxMenuItem = null

  //TODO i18n - do we need to change the shortcut keys too?
  setMnemonic('E')
  addMenuItem('Z', org.nlogo.editor.UndoManager.undoAction)
  addMenuItem('Y', org.nlogo.editor.UndoManager.redoAction)
  addSeparator()
  addMenuItem(I18N.gui("cut"), 'X', Actions.CUT_ACTION )
  addMenuItem(I18N.gui("copy"), 'C', Actions.COPY_ACTION)
  addMenuItem(I18N.gui("paste"), 'V', Actions.PASTE_ACTION)
  addMenuItem(I18N.gui("delete"), Actions.DELETE_ACTION)
  addSeparator()
  addMenuItem(I18N.gui("selectAll"), 'A', Actions.SELECT_ALL_ACTION)
  addSeparator()
  addMenuItem(I18N.gui("find"), 'F', FindDialog.FIND_ACTION)
  addMenuItem(I18N.gui("findNext"), 'G', FindDialog.FIND_NEXT_ACTION)
  addSeparator()
  val jumper = addMenuItem(I18N.gui("jumpToDefinition"), 'D',
                           Actions.jumpToDefinitionAction(new EditorColorizer(app.workspace),
                           I18N.gui.get _))
  addSeparator()
  addMenuItem(I18N.gui("shiftLeft"), '[', org.nlogo.editor.Actions.shiftLeftAction)
  addMenuItem(I18N.gui("shiftRight"), ']', org.nlogo.editor.Actions.shiftRightAction)
  addSeparator()
  addMenuItem(I18N.gui("comment"), ';', org.nlogo.editor.Actions.commentAction)
  addMenuItem(I18N.gui("uncomment"), ';', true, org.nlogo.editor.Actions.uncommentAction)
  addSeparator()
  snapper = addCheckBoxMenuItem(I18N.gui("snapToGrid"), app.workspace.snapOn(), snapAction)

  def handle(e: Events.SwitchedTabsEvent) {
    snapAction.setEnabled(e.newTab == app.tabs.interfaceTab)
    jumper.setEnabled(e.newTab!=app.tabs.infoTab)
  }

  def handle(e: org.nlogo.window.Events.LoadSectionEvent) {
    if(e.section == org.nlogo.api.ModelSection.ModelSettings) {
      app.workspace.snapOn(e.lines != null &&
                           e.lines.nonEmpty &&
                           e.lines.head.trim.nonEmpty &&
                           e.lines.head.toInt != 0)
      snapper.setState(app.workspace.snapOn)
    }
  }
}
