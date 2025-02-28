// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.sdm.ModelElement

trait ModelElementFigure {
  def getModelElement: ModelElement
  def dirty: Boolean
}
