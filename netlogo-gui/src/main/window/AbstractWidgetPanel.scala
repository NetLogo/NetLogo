// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ Widget => CoreWidget }

abstract class AbstractWidgetPanel
extends javax.swing.JLayeredPane
with Zoomable {
  def removeAllWidgets()
  def getWidgetsForSaving: Seq[CoreWidget]
  def loadWidgets(widgets: Seq[CoreWidget])
  def hasView: Boolean
  def empty: Boolean
}
