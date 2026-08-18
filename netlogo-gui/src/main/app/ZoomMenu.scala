// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.KeyEvent
import javax.swing.Action

import org.nlogo.core.I18N

// note that multiple instances of this class may exist as there are now multiple frames that each
// have their own menu bar and menus  ev 8/25/05
class ZoomMenu extends org.nlogo.swing.Menu(I18N.gui.get("menu.zoom")) {

  implicit val i18nName: org.nlogo.core.I18N.Prefix = I18N.Prefix("menu.zoom")

  setMnemonic('Z')
  addMenuItem('=',new javax.swing.AbstractAction(I18N.gui("larger")) {
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L)

    def actionPerformed(e:java.awt.event.ActionEvent): Unit = {
      zoom(1)
    }})
  addMenuItem('0',new javax.swing.AbstractAction(I18N.gui("normalSize")) {
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N)

    def actionPerformed(e:java.awt.event.ActionEvent): Unit = {
      zoom(0)
    }})
  addMenuItem('-',new javax.swing.AbstractAction(I18N.gui("smaller")) {
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S)

    def actionPerformed(e:java.awt.event.ActionEvent): Unit = {
      zoom(-1)
    }})

  def zoom(action: Int): Unit = {
    new org.nlogo.window.Events.ZoomedEvent(action).raise(this)
  }

}
