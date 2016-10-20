// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.{ Action, JMenuItem }
import org.nlogo.editor.{ Actions, Colorizer }
import org.nlogo.swing.BrowserLauncher
import org.nlogo.api.Version
import org.nlogo.core.I18N

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus - ev 8/25/05

class HelpMenu extends org.nlogo.swing.Menu(I18N.gui.get("menu.help")) {
  setMnemonic('H')

  def addEditorActions(actions: Seq[Action]): Unit = {
    // add the last item first, so that items get "pushed" to the appropriate spot
    actions.reverse.foreach { action =>
      add(new JMenuItem(action), 0)
    }
  }
}
