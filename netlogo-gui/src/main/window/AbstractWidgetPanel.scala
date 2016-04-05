// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader

abstract class AbstractWidgetPanel
extends javax.swing.JLayeredPane
with Zoomable {
  def removeAllWidgets()
  def getWidgetsForSaving: Seq[CoreWidget]
  def loadWidgets(widgets:Array[String], version:String, additionalReaders: Map[String, WidgetReader] = Map())
  def hasView: Boolean
  def empty: Boolean
}
