// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.core.I18N
import org.nlogo.swing.UserAction,
  UserAction.{ HelpAboutGroup, HelpContextGroup, HelpWebGroup }
import org.nlogo.window.Menu

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus - ev 8/25/05

object HelpMenu {
  def sortOrder = Seq(HelpContextGroup, HelpWebGroup, HelpAboutGroup)
}

class HelpMenu extends Menu(I18N.gui.get("menu.help"), Menu.model(HelpMenu.sortOrder)) {
  setMnemonic('H')
}
