// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.{JMenuItem, JPopupMenu, JTextField, MenuSelectionManager}

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
    val filterField = new JTextField()
    filterField.getDocument.addDocumentListener(new DocumentListener {
      override def removeUpdate(e: DocumentEvent): Unit = changedUpdate(e)
      override def insertUpdate(e: DocumentEvent): Unit = changedUpdate(e)
      override def changedUpdate(e: DocumentEvent): Unit = {
        val query = filterField.getText
        val caseSensitive = if (query == query.toLowerCase) "" else "(?i)"
        val pattern = caseSensitive + filterField.getText.split("").mkString(".*", ".*", ".*")
        menu.getSubElements.foreach {
          case it: JMenuItem => it.setEnabled(procs.nonEmpty && it.getText.matches(pattern))
          case _ =>
        }
        menu.getSubElements.collectFirst {
          case it: JMenuItem if it.isEnabled => it
        }.foreach(it => MenuSelectionManager.defaultManager().setSelectedPath(Array(menu, it)))
      }
    })

    filterField.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
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
    if(procs.isEmpty) menu.add(new JMenuItem("<"+I18N.gui.get("tabs.code.procedures.none")+">") { setEnabled(false) })
    else {
      for(proc <- procs.sortWith(_.toUpperCase < _.toUpperCase)) {
        val item = new JMenuItem(proc)
        val namePos = procsTable(proc).identifier.start
        val end  = procsTable(proc).endKeyword.end
        item.addActionListener{() =>
          // invokeLater for the scrolling behavior we want. we scroll twice: first bring the end into
          // view, then bring the beginning into view, so then we can see both, if they fit - ST 11/4/04
          target.select(end, end)
          EventQueue.invokeLater{() =>
            target.select(namePos, namePos + proc.size)  // highlight the name
          }
        }
        menu.add(item)
      }
    }
  }
}
