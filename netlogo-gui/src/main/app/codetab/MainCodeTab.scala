// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JCheckBox, JFrame }

import org.nlogo.api.ModelSection
import org.nlogo.app.common.{ Events => AppEvents, FindDialog, TabsInterface }
import org.nlogo.core.I18N
import org.nlogo.swing.{ ToolBar, ToolBarActionButton }
import org.nlogo.editor.EditorMenu
import org.nlogo.window.{ Events => WindowEvents, GUIWorkspace }
import org.nlogo.workspace.AbstractWorkspace

// This is THE Code tab.  Certain settings and things that are only accessible here.
// Other Code tabs come and go.

class MainCodeTab(workspace: GUIWorkspace, tabs: TabsInterface)
extends CodeTab(workspace)
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

  override def editorConfiguration = {
    val config = super.editorConfiguration
    workspace.getFrame match {
      case frame: JFrame => frame.getJMenuBar match {
        case em: EditorMenu => config.withMenu(em)
        case other          => config
      }
      case _ => config
    }
  }

  def smartTabbingEnabled = tabbing.isSelected

  override def getToolBar =
    new ToolBar {
      override def addControls() {
        add(new ToolBarActionButton(FindDialog.FIND_ACTION))
        add(new ToolBarActionButton(compileAction))
        add(new ToolBar.Separator)
        add(new ProceduresMenu(MainCodeTab.this))
        // we add this here, however, unless there are includes it will not be displayed, as it sets
        // it's preferred size to 0x0 -- CLB
        add(new IncludesMenu(MainCodeTab.this, tabs))
        // turning auto-indent checkbox back on
        add(new ToolBar.Separator)
        tabbing = new JCheckBox(smartTabAction)
        add(tabbing)
        // turning it on by default (for now, anyway ~Forrest)
        tabbing.setSelected(true)
        // hack, to get it to realize it's really checked. ~Forrest (10/23/2007)
        smartTabAction.actionPerformed(null)
      }
    }

  def handle(e: WindowEvents.LoadModelEvent) {
    innerSource = e.model.code
    recompile()
  }
}
