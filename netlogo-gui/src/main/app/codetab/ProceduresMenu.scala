// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.event.KeyEvent
import javax.swing.{JMenuItem, JPopupMenu, JTextField, MenuSelectionManager, SwingUtilities}

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.Implicits._
import org.nlogo.swing.ToolBarMenu

import scala.util.matching.Regex

class ProceduresMenu(target: ProceduresMenuTarget)
extends ToolBarMenu(I18N.gui.get("tabs.code.procedures")) {
  override def populate(menu: JPopupMenu) {
    val procsTable = {
      target.compiler.findProcedurePositions(target.getText)
    }
    // Procedures, sorted by appearance in code
    val procs = procsTable.keys.toSeq.sortBy(procsTable(_).identifier.start)

    val items = procs.map { proc =>
      val item = new JMenuItem(proc)
      val namePos = procsTable(proc).identifier.start
      val end  = procsTable(proc).endKeyword.end
      item.addActionListener { _ =>
        // invokeLater for the scrolling behavior we want. we scroll twice: first bring the end into
        // view, then bring the beginning into view, so then we can see both, if they fit - ST 11/4/04
        target.select(end, end)
        EventQueue.invokeLater{() =>
          target.select(namePos, namePos + proc.length)  // highlight the name
        }
      }
      item
    }

    val filterField = new JTextField
    filterField.getDocument.addDocumentListener(() => {
      repopulate(menu, filterField, items)
      menu.getSubElements.collectFirst {
        case it: JMenuItem => MenuSelectionManager.defaultManager.setSelectedPath(Array(menu, it))
        case _ =>
      }

      // Changing the number of items changes the desired size of the menu
      // -BCH 1/29/2018
      menu.validate() // recalculate correct size
      menu.setSize(menu.getPreferredSize) // resize to that size
      // Resize the popup to match. On Mac, the popup has is in its own undecorated window, so we resize root. On
      // Windows and Linux, the root of the menu is the app itself, but the parent is the popup, so resize that.
      // -BCH 1/29/2018
      Option(
        if (System.getProperty("os.name").startsWith("Mac")) SwingUtilities.getRoot(menu)
        else menu.getParent
      ).foreach(r => r.setSize(r.getPreferredSize))
    })

    filterField.addKeyListener { e: KeyEvent =>
      // Although it seems like you should just be able to do:
      // MenuSelectionManager.defaultManager().processKeyEvent(e)
      // here and have arrow keys and enter work, this is not the case.
      // Instead, we have to pass through the keyboard events to the menu itself to make arrow keys work.
      // We have to explicitly simulate the click on enter for enter to work.
      // I decided to pass through ALL keys instead of just the keys we care about because we can't guarantee that
      // menu manipulation keys are the same on all systems (nor that I know about all of them).
      // Furthermore, there are no detrimental effects of passing on the keyboard events. -BCH 1/29/2018
      menu.dispatchEvent(e)
      if (e.getKeyCode == KeyEvent.VK_ENTER) {
        val path = MenuSelectionManager.defaultManager().getSelectedPath
        if (path.nonEmpty) path.last match {
          case it: JMenuItem if it.isArmed =>
            it.doClick()
            menu.setVisible(false)
          case _ =>
        }
      }
    }

    menu.add(filterField)
    repopulate(menu, filterField, items)

    // Windows and Linux seem to ignore requests for focus made before the popup is visible (or even right after it
    // becomes the call for visibility is made), so we delay the request until the next loop of the EDT. Note this line
    // is unnecessary for Mac, which grants focus to the field by default.
    // - BCH 1/31/2018
    SwingUtilities.invokeLater(() => filterField.requestFocusInWindow())
  }

  private def repopulate(menu: JPopupMenu, filterField: JTextField, items: Seq[JMenuItem]): Unit = {
    val query = Regex.quote(filterField.getText)
    val caseSensitive = if (query == query.toLowerCase) "" else "(?i)"
    val pattern = caseSensitive + filterField.getText.split("").mkString(".*", ".*", ".*")

    // If the filterField is removed and readded, it will lose focus on every keypress in Windows and Linux. So we just
    // remove the menu items (which makes sense anyway).
    // - BCH 1/31/2018
    val curItems = menu.getSubElements.collect{case it: JMenuItem => it}
    curItems.foreach(menu.remove)
    val visibleItems = items.filter(_.getText matches pattern)
    if (visibleItems.isEmpty)
      menu.add(new JMenuItem("<"+I18N.gui.get("tabs.code.procedures.none")+">") { setEnabled(false) })
    else
      visibleItems.foreach(menu.add)
  }
}
