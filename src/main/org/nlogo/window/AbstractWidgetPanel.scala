// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

abstract class AbstractWidgetPanel extends javax.swing.JLayeredPane {
  def removeAllWidgets()
  def getWidgetsForSaving: java.util.List[org.nlogo.window.Widget]
  def loadWidgets(widgets:Array[String], version:String)
  def hasView: Boolean
  def empty: Boolean
}
