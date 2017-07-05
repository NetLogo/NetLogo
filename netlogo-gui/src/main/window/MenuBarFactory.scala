// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JMenu

trait MenuBarFactory {
  def createFileMenu:  JMenu
  def createEditMenu:  JMenu
  def createToolsMenu: JMenu
  def createZoomMenu:  JMenu
  def createHelpMenu:  JMenu
}
