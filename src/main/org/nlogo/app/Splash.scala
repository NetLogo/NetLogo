// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.Version

object Splash {

  private var splashWindow: javax.swing.JWindow = null

  def beginSplash() {
    splashWindow = new javax.swing.JWindow
    splashWindow.getContentPane.add(new MyIconHolder)
    splashWindow.pack()
    org.nlogo.awt.Positioning.center(splashWindow, null)
    splashWindow.setVisible(true)
  }

  def endSplash() {
    splashWindow.setVisible(false)
    splashWindow.dispose()
    splashWindow = null
  }

  def icon =
    new javax.swing.ImageIcon(
      Splash.getClass.getResource("/images/title.jpg"))

  class MyIconHolder extends org.nlogo.swing.IconHolder(icon) {
    val message = {
      val date = Version.buildDate
      val version = "Version " + Version.versionDropZeroPatch.drop("NetLogo ".size)
      // hopefully avoid confusion where semi-devel people report bugs in versions that aren't
      // finished yet -- don't foreground the version number to them - ST 2/27/06
      if(date.startsWith("INTERIM DEVEL BUILD"))
        date
      else version
    }
    override def paintComponent(g: java.awt.Graphics) {
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
