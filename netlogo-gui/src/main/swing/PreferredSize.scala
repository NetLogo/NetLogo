// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension }

trait PreferredSize extends Component {
  override def getMinimumSize: Dimension =
    getPreferredSize

  override def getMaximumSize: Dimension =
    getPreferredSize
}
