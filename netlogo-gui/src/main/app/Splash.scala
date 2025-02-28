// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Graphics, Rectangle }
import javax.swing.{ JLabel, JWindow }

import org.nlogo.api.Version
import org.nlogo.swing.Utils

object Splash {
  private var splashWindow: JWindow = null

  def beginSplash() {
    splashWindow = new JWindow
    splashWindow.getContentPane.add(splash)
    splashWindow.pack()
    org.nlogo.awt.Positioning.center(splashWindow, null)
    splashWindow.setVisible(true)
  }

  def endSplash() {
    splashWindow.setVisible(false)
    splashWindow.dispose()
    splashWindow = null
  }

  val image = Utils.iconScaled("/images/title.png", 600, 97)

  val splash = new JLabel(image) {
    val message = {
      val date = Version.buildDate
      val version = "Version " + Version.versionDropZeroPatch.drop("NetLogo ".size)
      // hopefully avoid confusion where semi-devel people report bugs in versions that aren't
      // finished yet -- don't foreground the version number to them - ST 2/27/06
      if(date.startsWith("INTERIM DEVEL BUILD"))
        date
      else version
    }

    override def paintComponent(g: Graphics) = {
      val g2d = Utils.initGraphics2D(g)
      super.paintComponent(g2d)
      val metrics = g2d.getFontMetrics
      val r = new Rectangle(getWidth - metrics.stringWidth(message) - 18,
                            getHeight - metrics.getHeight - 12,
                            metrics.stringWidth(message) + 12,
                            metrics.getHeight + 6)
      g2d.setColor(Color.WHITE)
      g2d.fillRect(r.x, r.y, r.width, r.height)
      g2d.setColor(Color.BLACK)
      g2d.drawRect(r.x, r.y, r.width, r.height)
      g2d.drawString(message,
                     getWidth - metrics.stringWidth(message) - 12,
                     getHeight - 12)
    }
  }
}
