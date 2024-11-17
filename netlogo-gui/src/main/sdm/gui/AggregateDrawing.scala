// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.jhotdraw.framework.Figure
import org.jhotdraw.standard.StandardDrawing

import org.nlogo.sdm.Model

class AggregateDrawing extends StandardDrawing {
  private val model = new Model("default", 1)

  def getModel: Model =
    model

  def synchronizeModel() {
    model.elements.clear()

    val figs = figures

    while (figs.hasNextFigure) {
      figs.nextFigure match {
        case f: ModelElementFigure if f.getModelElement != null =>
          model.addElement(f.getModelElement)
        case _ =>
      }
    }
  }

  override def orphan(figure: Figure): Figure = {
    figure match {
      case f: ModelElementFigure if f.getModelElement != null =>
        model.removeElement(f.getModelElement)
      case _ =>
    }

    super.orphan(figure)
  }
}
