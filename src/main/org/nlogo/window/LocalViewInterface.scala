// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait LocalViewInterface extends org.nlogo.api.ViewInterface {
  def getExportWindowFrame: java.awt.Component
  def exportView(): java.awt.image.BufferedImage
  def displaySwitch(on: Boolean): Unit
  def displaySwitch: Boolean
}
