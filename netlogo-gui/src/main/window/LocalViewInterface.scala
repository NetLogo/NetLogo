// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.awt.image.BufferedImage

import org.nlogo.api.{ ViewInterface => ApiViewInterface }

trait LocalViewInterface extends ApiViewInterface {
  def getExportWindowFrame: Component

  def exportView(): BufferedImage
}
