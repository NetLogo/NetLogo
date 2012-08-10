// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

abstract class AbstractWidgetPanel extends javax.swing.JLayeredPane {
  def removeAllWidgets()
  def getWidgetsForSaving: java.util.List[Widget]
  def loadWidgets(widgets: Seq[String], version: String)
  def hasView: Boolean
  def empty: Boolean
}
