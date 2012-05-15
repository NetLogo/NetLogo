// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

// Normally we create interfaces like these to resolve an inter-package dependency problem. In this
// case, we're only keeping WidgetPanel from depending on InterfaceToolbar, which maybe isn't
// strictly necessary since they're both in the app package, but it's still kind of nice. - ST
// 2/2/09

import org.nlogo.window.Widget

trait WidgetCreator {
  def getWidget: Widget
}
