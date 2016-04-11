// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ View => CoreView }

trait ViewWidgetInterface extends Widget {
  def asWidget: Widget = this

  def getAdditionalHeight: Int

  def load(view: CoreView): AnyRef
}
