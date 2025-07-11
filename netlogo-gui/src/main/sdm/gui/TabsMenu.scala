// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

// TODO i18n lot of work needed here...

import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, JTabbedPane }
import org.nlogo.swing.{ Menu, UserAction },
  UserAction.{ KeyBindings, MenuAction, TabsCategory }

object TabsMenu {
  def tabAction(tabs: JTabbedPane, index: Int): MenuAction =
    new AbstractAction(tabs.getTitleAt(index)) with MenuAction {
      category    = TabsCategory
      rank        = index
      accelerator = KeyBindings.keystroke(('1' + index).toChar, withMenu = true)
      override def actionPerformed(e: ActionEvent): Unit = {
        tabs.setSelectedIndex(index)
      }
    }

  def tabActions(tabs: JTabbedPane): Seq[MenuAction] =
    for (i <- 0 until tabs.getTabCount) yield tabAction(tabs, i)
}

class TabsMenu(name: String, initialActions: Seq[MenuAction]) extends Menu(name) {
  setMnemonic('A')

  initialActions.foreach(offerAction)

  def this(name: String) =
    this(name, Seq())

  def this(name: String, tabs: JTabbedPane) =
    this(name, TabsMenu.tabActions(tabs))
}
