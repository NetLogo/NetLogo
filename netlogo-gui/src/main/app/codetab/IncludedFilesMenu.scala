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
import org.nlogo.swing.{ FileDialog => SwingFileDialog, MenuItem, OptionPane, PopupMenu, RoundedBorderPanel,
                         ToolBarMenu }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Events => WindowEvents }

class IncludedFilesMenu(includesTable: => Option[Map[String, String]], tabs: TabsInterface)
extends ToolBarMenu(I18N.gui.get("tabs.code.includedFiles"))
with WindowEvents.CompiledEvent.Handler with RoundedBorderPanel with ThemeSync {
  implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tabs.code.includedFiles")

  private var alwaysVisible = NetLogoPreferences.get("includedFilesMenu", "false").toBoolean
  // If we're empty, we have no size, are invisible and don't affect our parent's layout
  private var isEmpty = true

  updateVisibility()

  setDiameter(6)
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
          includePaths.keys.toSeq.filter(include => include.endsWith(".nls") && new File(includePaths(include)).exists)

        if (filtered.isEmpty)
          menu.add(new MenuItem(I18N.gui.get("common.menus.empty"))).setEnabled(false)

        else {
          filtered.sortBy(_.toUpperCase(Locale.ENGLISH)).foreach(include =>
            menu.add(new MenuItem(new AbstractAction(include) {
              def actionPerformed(e: ActionEvent): Unit = {
                tabs.openExternalFile(includePaths(include))
              }
            })))
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

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground())
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
    setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
    setBorderColor(InterfaceColors.toolbarControlBorder())

    label.setForeground(InterfaceColors.toolbarText())
  }

  private class NewSourceEditorAction extends AbstractAction(I18N.gui("new")) {
    override def actionPerformed(e: ActionEvent) = tabs.newExternalFile()
  }

  private class OpenSourceEditorAction extends AbstractAction(I18N.gui("open") + Ellipsis) {
    override def actionPerformed(e: ActionEvent): Unit = ignoring(classOf[UserCancelException]) {
      val path = SwingFileDialog.showFiles(IncludedFilesMenu.this, I18N.gui("open"), FileDialog.LOAD, null)
        .replace(File.separatorChar, '/')
      if(path.endsWith(".nls"))
        tabs.openExternalFile(path)
      else
        new OptionPane(IncludedFilesMenu.this, I18N.gui.get("common.messages.error"),
                       I18N.gui.get("file.open.error.external.suffix"), OptionPane.Options.Ok, OptionPane.Icons.Error)
    }
  }
}
