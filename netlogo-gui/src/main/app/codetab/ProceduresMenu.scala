// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.{JMenuItem, JPopupMenu, JTextField, MenuSelectionManager, SwingUtilities}

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.Implicits._
import org.nlogo.swing.ToolBarMenu

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
      item.addActionListener{() =>
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
    filterField.getDocument.addDocumentListener(new DocumentListener {
      override def removeUpdate(e: DocumentEvent): Unit = changedUpdate(e)
      override def insertUpdate(e: DocumentEvent): Unit = changedUpdate(e)
      override def changedUpdate(e: DocumentEvent): Unit = {
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
      }
    })

    filterField.addKeyListener(new KeyAdapter {
      override def keyReleased(e: KeyEvent): Unit = keyPressed(e)
      override def keyTyped(e: KeyEvent): Unit = keyPressed(e)
      override def keyPressed(e: KeyEvent): Unit = {
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
    })

    menu.add(filterField)
    repopulate(menu, filterField, items)

    // Windows and Linux seem to ignore requests for focus made before the popup is visible (or even right after it
    // becomes the call for visibility is made), so we delay the request until the next loop of the EDT. Note this line
    // is unnecessary for Mac, which grants focus to the field by default.
    // - BCH 1/31/2018
    SwingUtilities.invokeLater(() => filterField.requestFocusInWindow())
  }

  private def repopulate(menu: JPopupMenu, filterField: JTextField, items: Seq[JMenuItem]): Unit = {
    val query = filterField.getText

    // If the filterField is removed and re-added (as would happen if we completely cleared the menu), it loses focus on
    // every keypress in Windows and Linux. So we just remove the menu items (which makes sense anyway).
    // - BCH 1/31/2018

    // This will include a <none> item if it's there
    menu.getSubElements.collect{case (it: JMenuItem) => it}.foreach(menu.remove)
    val visibleItems =
      if (query.isEmpty) items // So they aren't sorted
      else items.zip(items.map(it => fuzzyMatch(query, it.getText))) // score
        .filter(_._2 >= 0) // filter non-matches
        .sortWith { case ((it1, score1), (it2, score2)) => // sort
        score1 < score2 || (score1 == score2 && it1.getText.length < it2.getText.length)
      }.map(_._1)
    if (visibleItems.isEmpty)
      menu.add(new JMenuItem("<"+I18N.gui.get("tabs.code.procedures.none")+">") { setEnabled(false) })
    else
      visibleItems.foreach(menu.add)
  }

  private def fuzzyMatch(query: String, target: String): Int = {
    var i = 0
    var j = 0
    var score = 0
    while (i < query.length && j < target.length) {
      if (query(i) == target(j)) {
        i += 1
      } else {
        score += 1
      }
      j += 1
    }
    if (i < query.length) -1 else score
  }
}
