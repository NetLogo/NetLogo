// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

// any class that implements this needs to be a Widget

trait ViewWidgetInterface {
  def asWidget: Widget  // this should just return the object itself
  def getAdditionalHeight: Int
}
