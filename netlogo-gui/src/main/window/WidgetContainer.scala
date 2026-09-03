// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component

import org.nlogo.core.{ Widget => CoreWidget }

// implemented by WidgetPanel and InterfacePanelLite - ST 10/14/03

trait WidgetContainer extends Component {
  def loadWidget(coreWidget: CoreWidget): Widget
  def allWidgets: Seq[CoreWidget]
  def editWidget(widget: Editable): Unit
}
