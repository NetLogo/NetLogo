// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Font, FontMetrics, GraphicsEnvironment }

object Fonts {

  private def os(s: String) =
    System.getProperty("os.name").startsWith(s)

  lazy val platformFont =
    if (os("Mac"))
      "Lucida Grande"
    else
      "Sans-serif"

  lazy val platformMonospacedFont =
    if (os("Mac"))
      "Menlo"
    else if (os("Windows"))
      GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
        .find(_.equalsIgnoreCase("Lucida Console")).getOrElse("Monospaced")
    else "Monospaced"

  lazy val monospacedFont: Font =
    new Font(platformMonospacedFont, Font.PLAIN, 12)

  def adjustDefaultFont(comp: Component) {
    def plain(size: Int) =
      new Font(platformFont, Font.PLAIN, size)
    if (os("Mac"))
      comp.setFont(plain(11))
    else if (!os("Windows"))
      comp.setFont(plain(12))
  }

  def adjustDefaultMonospacedFont(comp: Component) {
    if (os("Mac"))
      comp.setFont(new Font(platformMonospacedFont, Font.PLAIN, 12))
  }

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
