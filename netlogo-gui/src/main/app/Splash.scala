// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Graphics
import javax.swing.{ JLabel, JWindow }

import org.nlogo.api.Version
import org.nlogo.swing.Utils.icon

object Splash {
  private var splashWindow: JWindow = null

  def beginSplash(version: Version = Version) {
    splashWindow = new JWindow
    splashWindow.getContentPane.add(new MyIconHolder(version))
    splashWindow.pack()
    org.nlogo.awt.Positioning.center(splashWindow, null)
    splashWindow.setVisible(true)
  }

  def endSplash() {
    splashWindow.setVisible(false)
    splashWindow.dispose()
    splashWindow = null
  }

  val image = icon("/images/title.jpg")

  class MyIconHolder(versionObj: Version) extends JLabel(image) {
    val message = {
      val date = versionObj.buildDate
      val version = "Version " + versionObj.versionDropZeroPatch.drop("NetLogo ".size)
      // hopefully avoid confusion where semi-devel people report bugs in versions that aren't
      // finished yet -- don't foreground the version number to them - ST 2/27/06
      if(date.startsWith("INTERIM DEVEL BUILD"))
        date
      else version
    }

    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      val metrics = g.getFontMetrics
      val r = new java.awt.Rectangle(getWidth - metrics.stringWidth(message) - 18,
                                     getHeight - metrics.getHeight - 12,
                                     metrics.stringWidth(message) + 12,
                                     metrics.getHeight + 6)
      g.setColor(java.awt.Color.WHITE)
      g.fillRect(r.x, r.y, r.width, r.height)
      g.setColor(java.awt.Color.BLACK)
      g.drawRect(r.x, r.y, r.width, r.height)
      g.drawString(message,
                   getWidth - metrics.stringWidth(message) - 12,
                   getHeight - 12)
    }
  }
}
