// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.{ Action, JMenu, JMenuBar }

import org.nlogo.core.I18N
import org.nlogo.editor.EditorMenu
import org.nlogo.swing.{ TabsMenu, UserAction },
  UserAction.{ ActionCategoryKey, EditCategory, FileCategory, HelpCategory, TabsCategory, ToolsCategory }

class MenuBar(isApplicationWide: Boolean)
  extends JMenuBar
  with EditorMenu
  with UserAction.Menu {

  val editMenu  = new EditMenu
  val fileMenu  = new FileMenu
  val tabsMenu  = new TabsMenu(I18N.gui.get("menu.tabs"))
  val toolsMenu = new ToolsMenu

  add(fileMenu)
  add(editMenu)
  add(toolsMenu)
  add(new ZoomMenu)
  add(tabsMenu)

  private var helpMenu = Option.empty[HelpMenu]

  private var categoryMenus: Map[String, UserAction.Menu] = Map(
    EditCategory  -> editMenu,
    FileCategory  -> fileMenu,
    ToolsCategory -> toolsMenu,
    TabsCategory  -> tabsMenu
  )

  override def setHelpMenu(newHelpMenu: JMenu): Unit = {
    newHelpMenu match {
      case hm: HelpMenu =>
        helpMenu = Some(hm)
        categoryMenus = categoryMenus + (HelpCategory -> hm)
      case _ =>
    }
    if (isApplicationWide) {
      try super.setHelpMenu(newHelpMenu)
      catch{
        // if not implemented in this VM (e.g. 1.8 on Mac as of right now),
        // then oh well - ST 6/23/03, 8/6/03 - RG 10/21/16
        case e: Error => org.nlogo.api.Exceptions.ignore(e)
      }
    }
  }

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
}

