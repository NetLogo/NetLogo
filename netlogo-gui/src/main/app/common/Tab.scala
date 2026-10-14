// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

trait Tab extends Component {
  def switchedTabs(oldTab: Component, newTab: Component): Unit
}
