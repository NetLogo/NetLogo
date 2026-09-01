// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Dimension, FileDialog }
import java.awt.event.ActionEvent
import java.io.File
import java.util.Locale
import javax.swing.AbstractAction

import scala.util.control.Exception.ignoring

import org.nlogo.app.common.{ Actions, TabsInterface }, Actions.Ellipsis
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.nvm.IncludeSource
import org.nlogo.swing.{ FileDialog => SwingFileDialog, MenuItem, OptionPane, PopupMenu, ToolBarMenu }
import org.nlogo.window.{ Events => WindowEvents }

class IncludedFilesMenu(includesTable: => Option[Map[String, IncludeSource]], tabs: TabsInterface)
  extends ToolBarMenu(I18N.gui.get("tabs.code.includedFiles")) with WindowEvents.CompiledEvent.Handler {

  implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tabs.code.includedFiles")

  private var alwaysVisible = NetLogoPreferences.get("includedFilesMenu", "false").toBoolean
  // If we're empty, we have no size, are invisible and don't affect our parent's layout
  private var isEmpty = true

  updateVisibility()
  enableHover()

  def setAlwaysVisible(visible: Boolean): Unit = {
    alwaysVisible = visible

    updateVisibility()
  }

  def handle(e: WindowEvents.CompiledEvent) = updateVisibility()

  def updateVisibility(): Unit = {
    isEmpty = includesTable.isEmpty
    revalidate()
    super.doLayout()
  }

  override def populate(menu: PopupMenu): Unit = {
    includesTable match {
      case Some(includePaths) =>
        val filtered =
          includePaths.filter((key, value) => (key.endsWith(".nls") || key.endsWith(".nlm")) &&
            new File(value.file).exists)

        if (filtered.isEmpty)
          menu.add(new MenuItem(I18N.gui.get("common.menus.empty"))).setEnabled(false)

        else {
          filtered.map(_.split(File.separatorChar).last -> _).toSeq.sortBy(_._1.toUpperCase(Locale.ENGLISH))
            .foreach((name, source) => menu.add(new MenuItem(name, () => tabs.openExternalFile(source.file))))
        }
      case None =>
        menu.add(new MenuItem(I18N.gui.get("common.menus.empty"))).setEnabled(false)
    }
    menu.addSeparator()
    menu.add(new MenuItem(new NewSourceEditorAction))
    menu.add(new MenuItem(new OpenSourceEditorAction))
  }

  private def sizeIfVisible(size: => Dimension) = if (alwaysVisible || !isEmpty) size else new Dimension(0,0)

  override def getMinimumSize = sizeIfVisible(super.getMinimumSize)
  override def getPreferredSize = sizeIfVisible(super.getPreferredSize)

  private class NewSourceEditorAction extends AbstractAction(I18N.gui("new")) {
    override def actionPerformed(e: ActionEvent) = tabs.newExternalFile()
  }

  private class OpenSourceEditorAction extends AbstractAction(I18N.gui("open") + Ellipsis) {
    override def actionPerformed(e: ActionEvent): Unit = ignoring(classOf[UserCancelException]) {
      val path = SwingFileDialog.showFiles(IncludedFilesMenu.this, I18N.gui("open"), FileDialog.LOAD, null)
        .replace(File.separatorChar, '/')
      if(path.endsWith(".nls") || path.endsWith(".nlm"))
        tabs.openExternalFile(path)
      else
        new OptionPane(IncludedFilesMenu.this, I18N.gui.get("common.messages.error"),
                       I18N.gui.get("file.open.error.external.suffix"), OptionPane.Options.Ok, OptionPane.Icons.Error)
    }
  }
}
