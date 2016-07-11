// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import javax.swing.{ JMenuItem, JPopupMenu }

import scala.collection.JavaConverters._

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.ToolBarMenu
import org.nlogo.swing.Implicits._

class ProceduresMenu(target: ProceduresMenuTarget)
        extends ToolBarMenu(I18N.gui.get("tabs.code.procedures")) {
  override def populate(menu: JPopupMenu) {
    val procsTable = {
      target.compiler.findProcedurePositions(target.getText)
    }
    val procs = procsTable.keys.toSeq
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
