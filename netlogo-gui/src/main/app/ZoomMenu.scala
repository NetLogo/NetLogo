// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.AbstractAction

import org.nlogo.api.ZoomRoot
import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ Menu, UserAction }, UserAction.{ KeyBindings, MenuAction }

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus  ev 8/25/05
class ZoomMenu(root: ZoomRoot) extends Menu(loc"Zoom") {
  setMnemonic('Z')

  addMenuItem(new AbstractAction(loc"Larger") with MenuAction {
    accelerator = KeyBindings.keystroke(KeyEvent.VK_PLUS, withMenu = true)
    mnemonic = KeyEvent.VK_L

    def actionPerformed(e: ActionEvent): Unit = {
      root.zoomIn()
    }
  })

  addMenuItem(new AbstractAction(loc"Normal Size") with MenuAction {
    accelerator = KeyBindings.keystroke(KeyEvent.VK_0, withMenu = true)
    mnemonic = KeyEvent.VK_N

    def actionPerformed(e: ActionEvent): Unit = {
      root.resetZoom()
    }
  })

  addMenuItem(new AbstractAction(loc"Smaller") with MenuAction {
    accelerator = KeyBindings.keystroke(KeyEvent.VK_MINUS, withMenu = true)
    mnemonic = KeyEvent.VK_S

    def actionPerformed(e: ActionEvent): Unit = {
      root.zoomOut()
    }
  })
}
