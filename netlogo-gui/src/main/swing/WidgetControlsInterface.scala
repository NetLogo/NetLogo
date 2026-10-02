// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component

trait WidgetControlsInterface extends Component {
  val widgetMenu: Component
  val toolButtons: Seq[Component]
}
