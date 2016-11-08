// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Dimension, FileDialog }
import java.awt.event.{ ActionEvent, ActionListener }
import java.io.File
import java.util.{ ArrayList => JArrayList, Collections }
import javax.swing.{ AbstractAction, JMenuItem, JOptionPane, JPopupMenu }

import scala.collection.JavaConverters._

import org.nlogo.api.Exceptions
import org.nlogo.app.common.TabsInterface
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.{ FileDialog => SwingFileDialog, ToolBarMenu }
import org.nlogo.window.{ Events => WindowEvents }

class IncludesMenu(target: CodeTab, tabs: TabsInterface)
extends ToolBarMenu("Includes")
with WindowEvents.CompiledEvent.Handler
{

  // If we're empty, we have no size, are invisible and don't affect our parent's layout
  private var isEmpty = true

  private var includesTable: Map[String, String] = null

  updateVisibility()

  def handle(e: WindowEvents.CompiledEvent) {
    if(e.sourceOwner.isInstanceOf[CodeTab])
      updateVisibility()
  }

  def updateVisibility() {
    isEmpty = target.getIncludesTable.isEmpty
    revalidate()
    super.doLayout()
  }

  override def populate(menu: JPopupMenu) {
    target.getIncludesTable match {
      case Some(includesTable) =>
        this.includesTable = includesTable
        val includes = new JArrayList[String]
        includesTable.keys.foreach(includes.add)
        Collections.sort(includes, String.CASE_INSENSITIVE_ORDER)
        for(include <- includes.asScala if include.endsWith(".nls") && include.size > 4)
          if(new File(includesTable(include)).exists) {
            val item = new JMenuItem(include)
            item.addActionListener(
              new ActionListener {
                override def actionPerformed(e: ActionEvent) {
                  menuSelection(include)
                }})
            menu.add(item)
          }
        menu.addSeparator()
      case None =>
        val nullItem = new JMenuItem("<No Includes Defined>")
        nullItem.setEnabled(false)
        menu.add(nullItem)
    }
    menu.add(new JMenuItem(new NewSourceEditorAction))
    menu.add(new JMenuItem(new OpenSourceEditorAction))
  }

  override def getMinimumSize =
    if (isEmpty) new Dimension(0, 0)
    else super.getMinimumSize

  override def getPreferredSize =
    if (isEmpty) new Dimension(0, 0)
    else super.getPreferredSize

  protected def menuSelection(s: String) {
    tabs.openExternalFile(includesTable(s))
  }

  private class OpenSourceEditorAction
  extends AbstractAction("Open Source File...")
  {
    override def actionPerformed(e: ActionEvent) {
      try {
        val path = SwingFileDialog.show(
            IncludesMenu.this, "Open: NetLogo Source File",
            FileDialog.LOAD, null)
          .replace(File.separatorChar, '/')
        if(path.endsWith(".nls"))
          tabs.openExternalFile(path)
        else
          JOptionPane.showMessageDialog(target, "Filename must end in *.nls")
      }
      catch {
        case ex: UserCancelException =>
          Exceptions.ignore(ex)
      }
    }
  }

  private class NewSourceEditorAction
  extends AbstractAction("New Source File") {
    override def actionPerformed(e: ActionEvent) {
      tabs.newExternalFile()
    }
  }

}
