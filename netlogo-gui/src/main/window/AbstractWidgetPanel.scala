// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLayeredPane

import org.nlogo.core.{ Widget => CoreWidget }
import org.nlogo.theme.ThemeSync

abstract class AbstractWidgetPanel extends JLayeredPane with Zoomable with ThemeSync {
  def removeAllWidgets()
  def getWidgetsForSaving: Seq[CoreWidget]
  def loadWidgets(widgets: Seq[CoreWidget])
  def snapWidgetBounds(): Unit
  def hasView: Boolean
  def empty: Boolean
}
