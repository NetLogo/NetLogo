// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JCheckBox }

import org.nlogo.app.common.{ Events => AppEvents, TabsInterface }
import org.nlogo.core.I18N
import org.nlogo.editor.EditorMenu
import org.nlogo.window.{ Events => WindowEvents, GUIWorkspace }

// This is THE Code tab.  Certain settings and things that are only accessible here.
// Other Code tabs come and go.

class MainCodeTab(workspace: GUIWorkspace, tabs: TabsInterface, editorMenu: EditorMenu)
extends CodeTab(workspace, tabs)
with WindowEvents.LoadModelEvent.Handler
{

  var tabbing: JCheckBox = null
  val smartTabAction: Action = new SmartTabAction

  private class SmartTabAction extends AbstractAction(I18N.gui.get("tabs.code.indentAutomatically")) {
    def actionPerformed(e: ActionEvent) {
      setIndenter(tabbing.isSelected)
      new AppEvents.IndenterChangedEvent(tabbing.isSelected)
        .raise(MainCodeTab.this)
    }
  }

  override def editorConfiguration =
    super.editorConfiguration.withMenu(editorMenu)

  def smartTabbingEnabled = tabbing.isSelected

  override def getAdditionalToolBarComponents: Seq[Component] = {
    tabbing = new JCheckBox(smartTabAction)
    // turning it on by default (for now, anyway ~Forrest)
    tabbing.setSelected(true)
    // hack, to get it to realize it's really checked. ~Forrest (10/23/2007)
    smartTabAction.actionPerformed(null)
    Seq(tabbing)
  }

  override def dirty_=(b: Boolean) = {
    super.dirty_=(b)
    if (b) {
      new WindowEvents.DirtyEvent(None).raise(this)
      new org.nlogo.window.Events.UpdateModelEvent(m => m.copy(code = innerSource)).raise(this)
    }
  }

  def handle(e: WindowEvents.LoadModelEvent) {
    innerSource = e.model.code
    compile()
  }
}
