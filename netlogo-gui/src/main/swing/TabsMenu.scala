// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

// TODO i18n lot of work needed here...

import java.awt.event.ActionEvent
import javax.swing.{ Action, AbstractAction }
import UserAction._
import org.nlogo.app.{ AbstractTabs, AppTabManager }

object TabsMenu {
  def tabAction(tabMgr: AppTabManager, index: Int): Action =
    new AbstractAction() with MenuAction {
      category    = TabsCategory
      rank        = index
      accelerator = KeyBindings.keystroke(('1' + index).toChar, withMenu = true)
      this.putValue(Action.NAME, tabMgr.getAppTabsPanel.asInstanceOf[AbstractTabs].getTitleAtCombinedIndex(index));
      override def actionPerformed(e: ActionEvent) {
        tabMgr.getAppTabsPanel.asInstanceOf[AbstractTabs].setSelectedIndexPanels(index)
      }
    }

  def tabActions(tabMgr: AppTabManager): Seq[Action] = {
    val totalTabCount = tabMgr.getCombinedTabCount
    for (i <- 0 until totalTabCount) yield tabAction(tabMgr, i)
  }

}

class TabsMenu(name: String, initialActions: Seq[Action]) extends Menu(name) {
  setMnemonic('A')

  initialActions.foreach(offerAction)

  def this(name: String) =
    this(name, Seq())

  def this(name: String, tabMgr: AppTabManager) =
    this(name, TabsMenu.tabActions(tabMgr))
}
