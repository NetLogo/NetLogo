// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.JMenu

import
  org.nlogo.swing.UserAction,
    UserAction.{ ActionCategoryKey, EditCategory, FileCategory, HelpCategory, ToolsCategory }

import
  org.nlogo.window.{ MenuBarFactory => WindowMenuBarFactory }

// This is for other windows to get their own copy of the menu
// bar.  It's needed especially for OS X since the screen menu bar
// doesn't get shared across windows.  -- AZS 6/17/2005
trait MenuBarFactory extends WindowMenuBarFactory {
  def actions: Seq[javax.swing.Action]

  def createMenu(newMenu: org.nlogo.swing.Menu, category: String): JMenu = {
    actions.filter(_.getValue(ActionCategoryKey) == category).foreach(newMenu.offerAction)
    newMenu
  }

  def createEditMenu:  JMenu = createMenu(new EditMenu,  EditCategory)
  def createFileMenu:  JMenu = createMenu(new FileMenu,  FileCategory)
  def createHelpMenu:  JMenu = createMenu(new HelpMenu,  HelpCategory)
  def createToolsMenu: JMenu = createMenu(new ToolsMenu, ToolsCategory)
  def createZoomMenu:  JMenu = new ZoomMenu
}

class StatefulMenuBarFactory extends MenuBarFactory {
  var actions: Seq[javax.swing.Action] = Seq()
}

