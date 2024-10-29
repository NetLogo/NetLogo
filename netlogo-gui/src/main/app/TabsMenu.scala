// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.ActionEvent
import javax.swing.{ Action, AbstractAction }

import org.nlogo.swing.UserAction._

object TabsMenu {
  def tabAction(tabManager: TabManager, index: Int): Action =
    new AbstractAction(tabManager.getTabTitle(index)) with MenuAction {
      category    = TabsCategory
      rank        = index
      accelerator = KeyBindings.keystroke(('1' + index).toChar, withMenu = true)
      override def actionPerformed(e: ActionEvent) {
        tabManager.setSelectedIndex(index)
      }
    }

  def tabActions(tabManager: TabManager): Seq[Action] = {
    val totalTabCount = tabManager.getTotalTabCount
    for (i <- 0 until totalTabCount) yield tabAction(tabManager, i)
  }

}

class TabsMenu(name: String, initialActions: Seq[Action]) extends org.nlogo.window.Menu(name) {
  setMnemonic('A')

  initialActions.foreach(offerAction)

  def this(name: String) =
    this(name, Seq())

  def this(name: String, tabManager: TabManager) =
    this(name, TabsMenu.tabActions(tabManager))
}
