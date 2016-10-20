// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ JMenu, JMenuBar }

trait MenuBarFactory {
  def createFileMenu: JMenu

  def createEditMenu: JMenu

  def createToolsMenu: JMenu

  def createZoomMenu: JMenu

  def addHelpMenu(menuBar: JMenuBar): JMenu
}
