// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.api.{ ModelSection}
import org.nlogo.core.I18N

// This is THE Code tab.  Certain settings and things that are only accessible here.
// Other Code tabs come and go.

class MainCodeTab(workspace: AbstractWorkspace)
extends CodeTab(workspace)
with org.nlogo.window.Events.LoadSectionEvent.Handler
{

  var tabbing: javax.swing.JCheckBox = null
  val smartTabAction: javax.swing.Action = new SmartTabAction

  private class SmartTabAction extends javax.swing.AbstractAction(I18N.gui.get("tabs.code.indentAutomatically")) {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      setIndenter(tabbing.isSelected)
      new org.nlogo.app.Events.IndenterChangedEvent(tabbing.isSelected)
        .raise(MainCodeTab.this)
    }
  }

  def smartTabbingEnabled = tabbing.isSelected

  override def getToolBar() =
    new org.nlogo.swing.ToolBar() {
      override def addControls() {
        add(new javax.swing.JButton(
          org.nlogo.app.FindDialog.FIND_ACTION))
        add(new javax.swing.JButton(compileAction))
        add(new org.nlogo.swing.ToolBar.Separator)
        add(new ProceduresMenu(MainCodeTab.this))
        // we add this here, however, unless there are includes it will not be displayed, as it sets
        // it's preferred size to 0x0 -- CLB
        add(new IncludesMenu(MainCodeTab.this))
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
      innerSource(workspace.autoConvert(e.text, false, false, e.version))
      recompile()
    }
  }
}
