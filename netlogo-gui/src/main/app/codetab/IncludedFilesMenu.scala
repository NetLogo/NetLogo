// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Dimension, FileDialog }
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.{ AbstractAction, JMenuItem, JOptionPane, JPopupMenu }

import scala.util.control.Exception.ignoring

import org.nlogo.app.common.{ Actions, TabsInterface }, Actions.Ellipsis
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ FileDialog => SwingFileDialog, RichJMenuItem, ToolBarMenu }
import org.nlogo.window.{ Events => WindowEvents }

class IncludedFilesMenu(target: CodeTab, tabs: TabsInterface)
extends ToolBarMenu(I18N.gui.get("tabs.code.includedFiles"))
with WindowEvents.CompiledEvent.Handler
{
  implicit val i18nPrefix = I18N.Prefix("tabs.code.includedFiles")

  // If we're empty, we have no size, are invisible and don't affect our parent's layout
  private var isEmpty = true

  updateVisibility()

  def handle(e: WindowEvents.CompiledEvent) = updateVisibility()

  def updateVisibility(): Unit = {
    isEmpty = target.getIncludesTable.isEmpty
    revalidate()
    super.doLayout()
  }

  override def populate(menu: JPopupMenu) = {
    target.getIncludesTable match {
      case Some(includesTable) =>
        includesTable.keys.toSeq
          .filter(include => include.endsWith(".nls") && new File(includesTable(include)).exists)
          .sortBy(_.toUpperCase)
          .foreach(include => menu.add(RichJMenuItem(include) {
            tabs.openExternalFile(includesTable(include))
          }))
        menu.addSeparator()
      case None =>
        val nullItem = new JMenuItem(I18N.gui.get("common.menus.empty"))
        nullItem.setEnabled(false)
        menu.add(nullItem)
    }
    menu.add(new JMenuItem(NewSourceEditorAction))
    menu.add(new JMenuItem(OpenSourceEditorAction))
  }

  private def sizeIfVisible(size: => Dimension) = if (isEmpty) new Dimension(0,0) else size

  override def getMinimumSize = sizeIfVisible(super.getMinimumSize)
  override def getPreferredSize = sizeIfVisible(super.getPreferredSize)

  private object NewSourceEditorAction extends AbstractAction(I18N.gui("new")) {
    override def actionPerformed(e: ActionEvent) = tabs.newExternalFile()
  }

  private object OpenSourceEditorAction extends AbstractAction(I18N.gui("open") + Ellipsis) {
    override def actionPerformed(e: ActionEvent) = ignoring(classOf[UserCancelException]) {
      val path = SwingFileDialog.show(IncludedFilesMenu.this, I18N.gui("open"), FileDialog.LOAD, null)
        .replace(File.separatorChar, '/')
      if(path.endsWith(".nls"))
        tabs.openExternalFile(path)
      else
        JOptionPane.showMessageDialog(target, I18N.gui.get("file.open.error.external.suffix"))
    }
  }
}
