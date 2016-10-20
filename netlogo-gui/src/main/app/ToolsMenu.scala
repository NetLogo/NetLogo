// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that
 each have their own menu bar and menus ev 8/25/05 */

import javax.swing.{ Action, JMenuItem, JSeparator }

import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.UserAction
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.WorkspaceActions.HaltGroup
import org.nlogo.swing.{ Menu, UserAction}, UserAction.{ ToolsDialogsGroup, ToolsMonitorGroup, ToolsHubNetGroup }

object ToolsMenu {
  val sortOrder = Seq(HaltGroup, ToolsMonitorGroup, InterfaceTab.MenuGroup, ToolsDialogsGroup, ToolsHubNetGroup)
}

class ToolsMenu
  extends org.nlogo.swing.Menu(I18N.gui.get("menu.tools"), Menu.model(ToolsMenu.sortOrder))
  with UserAction.Menu {

  setMnemonic('T')
}
