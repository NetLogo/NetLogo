// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLayeredPane

import org.nlogo.core.{ Widget => CoreWidget }
import org.nlogo.theme.ThemeSync

abstract class AbstractWidgetPanel extends JLayeredPane with Zoomable with ThemeSync {
  def removeAllWidgets(): Unit
  def getWidgetsForSaving: Seq[CoreWidget]
  def loadWidgets(widgets: Seq[CoreWidget], widgetSizesOption: WidgetSizes): Unit
  def convertWidgetSizes(reposition: Boolean): Unit
  def hasView: Boolean
  def empty: Boolean
  def setBoldWidgetText(value: Boolean): Unit
}
