// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait ViewWidgetInterface extends Widget {
  def asWidget: Widget = this

  def getAdditionalHeight: Int
}
