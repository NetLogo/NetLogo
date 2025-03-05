// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.{ JLabel, JWindow }

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

  val splash = new JLabel(Utils.iconScaled("/images/banner.png", 600, 231))
}
