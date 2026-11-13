// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that
 each have their own menu bar and menus ev 8/25/05 */

import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ Menu, UserAction }, UserAction.{ ToolsDialogsGroup, ToolsMonitorGroup, ToolsHubNetGroup,
                                                          ToolsSettingsGroup, ToolsWidgetGroup }
import org.nlogo.window.WorkspaceActions,
  WorkspaceActions.HaltGroup

object ToolsMenu {
  val sortOrder = Seq(ToolsSettingsGroup, HaltGroup, ToolsMonitorGroup, InterfaceTab.MenuGroup, ToolsDialogsGroup,
                      ToolsHubNetGroup, ToolsWidgetGroup)
}

class ToolsMenu
  extends Menu(loc"Tools", Menu.model(ToolsMenu.sortOrder))
  with UserAction.Menu {

  setMnemonic('T')
}
