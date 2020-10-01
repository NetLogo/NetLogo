// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

// TODO i18n lot of work needed here...

import java.awt.event.ActionEvent
import javax.swing.{ Action, AbstractAction, JTabbedPane }
import UserAction._
import org.nlogo.app.AbstractTabs

object TabsMenu {
  def tabAction(tabs: JTabbedPane, index: Int): Action =
    new AbstractAction() with MenuAction {
      category    = TabsCategory
      rank        = index
      accelerator = KeyBindings.keystroke(('1' + index).toChar, withMenu = true)
      this.putValue(Action.NAME, tabs.asInstanceOf[AbstractTabs].getTitleAtAdjusted(index));
      override def actionPerformed(e: ActionEvent) {
        tabs.asInstanceOf[AbstractTabs].setSelectedIndexPanels(index)
      }
    }

  def tabActions(tabs: JTabbedPane): Seq[Action] = {
    val totalTabCount = tabs.asInstanceOf[AbstractTabs].getTabManager.getTotalTabCount
    for (i <- 0 until totalTabCount) yield tabAction(tabs, i)
  }

}

class TabsMenu(name: String, initialActions: Seq[Action]) extends Menu(name) {
  setMnemonic('A')

  initialActions.foreach(offerAction)

  def this(name: String) =
    this(name, Seq())

  def this(name: String, tabs: JTabbedPane) =
    this(name, TabsMenu.tabActions(tabs))
}
