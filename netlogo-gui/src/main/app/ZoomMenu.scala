// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.AbstractAction

import org.nlogo.core.I18N
import org.nlogo.swing.{ Menu, UserAction }, UserAction.MenuAction

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus  ev 8/25/05
class ZoomMenu extends Menu(I18N.gui.get("menu.zoom")) {
  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("menu.zoom")

  setMnemonic('Z')

  addMenuItem('=',new AbstractAction(I18N.gui("larger")) with MenuAction {
    mnemonic = KeyEvent.VK_L

    def actionPerformed(e: ActionEvent): Unit = {
      App.app.frame.zoomIn()
    }
  })

  addMenuItem('0',new AbstractAction(I18N.gui("normalSize")) with MenuAction {
    mnemonic = KeyEvent.VK_N

    def actionPerformed(e: ActionEvent): Unit = {
      App.app.frame.resetZoom()
    }
  })

  addMenuItem('-',new AbstractAction(I18N.gui("smaller")) with MenuAction {
    mnemonic = KeyEvent.VK_S

    def actionPerformed(e: ActionEvent): Unit = {
      App.app.frame.zoomOut()
    }
  })
}
