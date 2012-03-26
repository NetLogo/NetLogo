// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that
 each have their own menu bar and menus ev 8/25/05 */

import org.nlogo.agent.{Observer, Turtle, Patch, Link}
import org.nlogo.api.I18N

class ToolsMenu(app: App) extends org.nlogo.swing.Menu(I18N.gui.get("menu.tools")) {

  implicit val i18nName = I18N.Prefix("menu.tools")
  
  addMenuItem(I18N.gui("halt"), app.workspace.halt _)
  addSeparator()
  addMenuItem('/', app.tabs.interfaceTab.commandCenterAction)
}
