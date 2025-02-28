// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Graphics
import javax.swing.{ Action, JMenuBar }

import org.nlogo.core.I18N
import org.nlogo.editor.EditorMenu
import org.nlogo.swing.{ UserAction, Utils },
  UserAction.{ ActionCategoryKey, EditCategory, FileCategory, HelpCategory, TabsCategory, ToolsCategory }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class MenuBar(isApplicationWide: Boolean) extends JMenuBar with EditorMenu with UserAction.Menu with ThemeSync {
  val editMenu  = new EditMenu
  val fileMenu  = new FileMenu
  val tabsMenu  = new TabsMenu(I18N.gui.get("menu.tabs"))
  val toolsMenu = new ToolsMenu
  val helpMenu  = new HelpMenu
  val zoomMenu = new ZoomMenu

  add(fileMenu)
  add(editMenu)
  add(toolsMenu)
  add(zoomMenu)
  add(tabsMenu)
  add(helpMenu)

  if (isApplicationWide) {
    try super.setHelpMenu(helpMenu)
    catch {
      // if not implemented in this VM (e.g. 1.8 on Mac as of right now),
      // then oh well - ST 6/23/03, 8/6/03 - RG 10/21/16
      case e: Error => org.nlogo.api.Exceptions.ignore(e)
    }
  }

  private val categoryMenus: Map[String, UserAction.Menu] = Map(
    EditCategory  -> editMenu,
    HelpCategory  -> helpMenu,
    FileCategory  -> fileMenu,
    ToolsCategory -> toolsMenu,
    TabsCategory  -> tabsMenu
  )

  def offerAction(action: javax.swing.Action): Unit = {
    val categoryKey = action.getValue(ActionCategoryKey) match {
      case s: String => s
      case _ => ""
    }
    categoryMenus.get(categoryKey).foreach(_.offerAction(action))
  }

  def revokeAction(action: Action): Unit = {
    val categoryKey = action.getValue(ActionCategoryKey) match {
      case s: String => s
      case _ => ""
    }
    categoryMenus.get(categoryKey).foreach(_.revokeAction(action))
  }

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.menuBackground)
    g2d.fillRect(0, 0, getWidth, getHeight)
  }

  override def paintBorder(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.menuBarBorder)
    g2d.drawLine(0, getHeight - 1, getWidth, getHeight - 1)
  }

  override def syncTheme(): Unit = {
    fileMenu.syncTheme()
    editMenu.syncTheme()
    toolsMenu.syncTheme()
    zoomMenu.syncTheme()
    tabsMenu.syncTheme()
    helpMenu.syncTheme()
  }
}
