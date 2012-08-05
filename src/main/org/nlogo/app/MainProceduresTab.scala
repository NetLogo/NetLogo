// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.api.{I18N, ModelSection}

// This is THE Code tab.  Certain settings and things that are only accessible here.
// Other Code tabs come and go.

class MainProceduresTab(workspace: AbstractWorkspace)
extends ProceduresTab(workspace)
with org.nlogo.window.Events.LoadSectionEventHandler
{

  var tabbing: javax.swing.JCheckBox = null
  val smartTabAction: javax.swing.Action = new SmartTabAction

  private class SmartTabAction extends javax.swing.AbstractAction(I18N.gui.get("tabs.code.indentAutomatically")) {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      setIndenter(tabbing.isSelected)
      new Events.IndenterChangedEvent(tabbing.isSelected)
        .raise(MainProceduresTab.this)
    }
  }

  def smartTabbingEnabled = tabbing.isSelected

  override def getToolBar() =
    new org.nlogo.swing.ToolBar() {
      override def addControls() {
        add(new javax.swing.JButton(
          FindDialog.FIND_ACTION))
        add(new javax.swing.JButton(compileAction))
        add(new org.nlogo.swing.ToolBar.Separator)
        add(new ProceduresMenu(MainProceduresTab.this))
        // turning auto-indent checkbox back on
        add(new org.nlogo.swing.ToolBar.Separator)
        tabbing = new javax.swing.JCheckBox(smartTabAction)
        add(tabbing)
        // turning it on by default (for now, anyway ~Forrest)
        tabbing.setSelected(true)
        // hack, to get it to realize it's really checked. ~Forrest (10/23/2007)
        smartTabAction.actionPerformed(null)
      }
    }

  def handle(e: org.nlogo.window.Events.LoadSectionEvent) {
    if(e.section == ModelSection.Code) {
      innerSource(e.text)
      recompile()
    }
  }
}
