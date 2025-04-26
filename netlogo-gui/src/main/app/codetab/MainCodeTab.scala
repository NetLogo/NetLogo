// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.app.common.TabsInterface
import org.nlogo.editor.EditorMenu
import org.nlogo.window.{ Events => WindowEvents, GUIWorkspace }

// This is THE Code tab.  Certain settings and things that are only accessible here.
// Other Code tabs come and go.

class MainCodeTab(workspace: GUIWorkspace, tabs: TabsInterface, editorMenu: EditorMenu)
extends CodeTab(workspace, tabs)
with WindowEvents.LoadModelEvent.Handler
{
  override def editorConfiguration =
    super.editorConfiguration.withMenu(editorMenu)

  override def dirty_=(b: Boolean) = {
    super.dirty_=(b)
    if (b) {
      new WindowEvents.DirtyEvent(None).raise(this)
    }
  }

  def handle(e: WindowEvents.LoadModelEvent): Unit = {
    innerSource = e.model.code
    compile()
  }
}
