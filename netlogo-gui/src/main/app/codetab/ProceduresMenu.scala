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
    val procs = procsTable.keys.toSeq

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

    val filterField = new JTextField()
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
        menu.validate() // recalculate correct size
        menu.setSize(menu.getPreferredSize) // resize to that size
        val root = SwingUtilities.getRoot(menu)
          root.setSize(root.getPreferredSize) // resize the window to match
        // All of the above is necessary.
        // -BCH 1/29/2018
      }
    })

    filterField.addKeyListener(new KeyAdapter {
      override def keyReleased(e: KeyEvent): Unit = keyPressed(e)
      override def keyTyped(e: KeyEvent): Unit = keyPressed(e)
      override def keyPressed(e: KeyEvent): Unit = {
        // TBH, it seems like you should just be able to do:
        // MenuSelectionManager.defaultManager().processKeyEvent(e)
        // here and it should work. Not sure why it doesn't, but meh. -BCH 1/29/2018
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

    repopulate(menu, filterField, items)
  }

  private def repopulate(menu: JPopupMenu, filterField: JTextField, items: Seq[JMenuItem]): Unit = {
    val query = filterField.getText
    val caseSensitive = if (query == query.toLowerCase) "" else "(?i)"
    val pattern = caseSensitive + filterField.getText.split("").mkString(".*", ".*", ".*")
    menu.removeAll()
    menu.add(filterField)
    items.filter(_.getText matches pattern).foreach(menu.add)

    val visibleItems = items.filter(_.getText matches pattern)
    if (visibleItems.isEmpty)
      menu.add(new JMenuItem("<"+I18N.gui.get("tabs.code.procedures.none")+">") { setEnabled(false) })
    else
      visibleItems.foreach(menu.add)

  }
}
