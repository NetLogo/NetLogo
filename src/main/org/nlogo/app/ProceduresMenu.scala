// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.awt.EventQueue
import org.nlogo.swing.Implicits._
import org.nlogo.api.I18N
import javax.swing.JMenuItem

class ProceduresMenu(target: ProceduresMenuTarget)
        extends org.nlogo.swing.ToolBarMenu(I18N.gui.get("tabs.code.procedures")) {
  override def populate(menu: javax.swing.JPopupMenu) {
    val procsTable =
      target.compiler.findProcedurePositions(target.getText)
    val procs = procsTable.values.map(_._1).toSeq
    if(procs.isEmpty)
      menu.add(new JMenuItem("<"+I18N.gui.get("tabs.code.procedures.none")+">") { setEnabled(false) })
    else {
      for(proc <- procs.sortWith(_.toUpperCase < _.toUpperCase)) {
        val item = new JMenuItem(proc)
        val namePos = procsTable(proc)._3
        val endPos  = procsTable(proc)._4
        item.addActionListener{() =>
          // invokeLater for the scrolling behavior we want. we scroll twice: first bring the end into
          // view, then bring the beginning into view, so then we can see both, if they fit - ST 11/4/04
          target.select(endPos, endPos)
          EventQueue.invokeLater{() =>
            target.select(namePos, namePos + proc.size)  // highlight the name
          }
        }
        menu.add(item)
      }
    }
  }
}
