// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Dimension, FileDialog }
import java.awt.event.ActionEvent
import java.io.File
import java.util.prefs.Preferences
import javax.swing.{ AbstractAction, JMenuItem, JOptionPane, JPopupMenu }

import scala.util.control.Exception.ignoring

import org.nlogo.app.common.{ Actions, TabsInterface }, Actions.Ellipsis
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ FileDialog => SwingFileDialog, RichJMenuItem, ToolBarMenu }
import org.nlogo.window.{ Events => WindowEvents, InterfaceColors, RoundedBorderPanel, ThemeSync }

class IncludedFilesMenu(includesTable: => Option[Map[String, String]], tabs: TabsInterface)
extends ToolBarMenu(I18N.gui.get("tabs.code.includedFiles"))
with WindowEvents.CompiledEvent.Handler with RoundedBorderPanel with ThemeSync {
  implicit val i18nPrefix = I18N.Prefix("tabs.code.includedFiles")

  val alwaysVisible = Preferences.userRoot.node("/org/nlogo/NetLogo").get("includedFilesMenu", "false").toBoolean
  // If we're empty, we have no size, are invisible and don't affect our parent's layout
  private var isEmpty = true

  updateVisibility()

  setDiameter(6)

  def handle(e: WindowEvents.CompiledEvent) = updateVisibility()

  def updateVisibility(): Unit = {
    isEmpty = includesTable.isEmpty
    revalidate()
    super.doLayout()
  }

  override def populate(menu: JPopupMenu) = {
    includesTable match {
      case Some(includePaths) =>
        includePaths.keys.toSeq
          .filter(include => include.endsWith(".nls") && new File(includePaths(include)).exists)
          .sortBy(_.toUpperCase)
          .foreach(include => menu.add(RichJMenuItem(include) {
            tabs.openExternalFile(includePaths(include))
          }))
      case None =>
        val nullItem = new JMenuItem(I18N.gui.get("common.menus.empty"))
        nullItem.setEnabled(false)
        menu.add(nullItem)
    }
    menu.addSeparator()
    menu.add(new JMenuItem(NewSourceEditorAction))
    menu.add(new JMenuItem(OpenSourceEditorAction))
  }

  private def sizeIfVisible(size: => Dimension) = if (alwaysVisible || !isEmpty) size else new Dimension(0,0)

  override def getMinimumSize = sizeIfVisible(super.getMinimumSize)
  override def getPreferredSize = sizeIfVisible(super.getPreferredSize)

  def syncTheme() {
    setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)

    label.setForeground(InterfaceColors.TOOLBAR_TEXT)
    
    arrow.setColor(InterfaceColors.TOOLBAR_TEXT)
  }

  private object NewSourceEditorAction extends AbstractAction(I18N.gui("new")) {
    override def actionPerformed(e: ActionEvent) = tabs.newExternalFile()
  }

  private object OpenSourceEditorAction extends AbstractAction(I18N.gui("open") + Ellipsis) {
    override def actionPerformed(e: ActionEvent) = ignoring(classOf[UserCancelException]) {
      val path = SwingFileDialog.showFiles(IncludedFilesMenu.this, I18N.gui("open"), FileDialog.LOAD, null)
        .replace(File.separatorChar, '/')
      if(path.endsWith(".nls"))
        tabs.openExternalFile(path)
      else
        JOptionPane.showMessageDialog(IncludedFilesMenu.this, I18N.gui.get("file.open.error.external.suffix"))
    }
  }
}
