// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.FontMetrics

object Fonts {

  /**
   * Squeezes a string to fit in a small space.
   */
  def shortenStringToFit(_name: String, availableWidth: Int, metrics: FontMetrics): String = {
    var name = _name
    if (metrics.stringWidth(name) > availableWidth) {
      name += "..."
      while (metrics.stringWidth(name) > availableWidth && name.size > 3)
        name = name.dropRight(4) + "..."
    }
    name
  }

}
