// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.StandardDrawing;
import org.nlogo.sdm.Model;

public strictfp class AggregateDrawing
    extends StandardDrawing {

  private final Model model = new Model("default", 1);

  public Model getModel() {
    return model;
  }

  @Override
  public Figure orphan(Figure figure) {
    if (figure instanceof ModelElementFigure &&
        ((ModelElementFigure) figure).getModelElement() != null) {
      ModelElementFigure fig = ((ModelElementFigure) figure);
      model.removeElement(fig.getModelElement());
    }
    return super.orphan(figure);
  }

}
