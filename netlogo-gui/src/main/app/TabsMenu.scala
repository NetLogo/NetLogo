// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.swing.{ Menu, UserAction },
  UserAction.{ KeyBindings, MenuAction, TabsCategory }

object TabsMenu {
  def tabAction(tabManager: TabManager, index: Int): MenuAction =
    new AbstractAction(tabManager.getTabTitle(index)) with MenuAction {
      category    = TabsCategory
      rank        = index
      accelerator = KeyBindings.keystroke(('1' + index).toChar, withMenu = true)
      override def actionPerformed(e: ActionEvent): Unit = {
        tabManager.setSelectedIndex(index)
      }
    }

  def tabActions(tabManager: TabManager): Seq[MenuAction] = {
    val totalTabCount = tabManager.getTotalTabCount
    for (i <- 0 until totalTabCount) yield tabAction(tabManager, i)
  }

}

class TabsMenu(name: String, initialActions: Seq[MenuAction]) extends Menu(name) {
  setMnemonic('A')

  initialActions.foreach(offerAction)

  def this(name: String) =
    this(name, Seq())

  def this(name: String, tabManager: TabManager) =
    this(name, TabsMenu.tabActions(tabManager))
}
