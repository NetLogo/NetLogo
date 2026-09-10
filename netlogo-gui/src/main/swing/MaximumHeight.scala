// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension }

trait MaximumHeight extends Component {
  override def getMaximumSize: Dimension =
    new Dimension(super.getMaximumSize.width, getPreferredSize.height)
}
