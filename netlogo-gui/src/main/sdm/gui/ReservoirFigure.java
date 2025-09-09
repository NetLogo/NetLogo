// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.jhotdraw.figures.EllipseFigure;
import org.jhotdraw.framework.FigureAttributeConstant;
import org.jhotdraw.framework.Handle;
import org.jhotdraw.framework.HandleEnumeration;
import org.jhotdraw.standard.HandleEnumerator;

import org.nlogo.sdm.Reservoir;

public class ReservoirFigure
    extends EllipseFigure
    implements ModelElementFigure {
  private final org.nlogo.sdm.Reservoir reservoir;

  public static ReservoirFigure create() {
    ReservoirFigure figure = new ReservoirFigure();

    figure.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.LIGHT_GRAY);

    return figure;
  }

  private ReservoirFigure() {
    reservoir = new Reservoir();
  }

  public org.nlogo.sdm.ModelElement getModelElement() {
    return reservoir;
  }

  @Override
  public void draw(java.awt.Graphics g) {
    ((java.awt.Graphics2D) g).setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
  }

  // no resize handles
  @Override
  public HandleEnumeration handles() {
    return new HandleEnumerator(new ArrayList<Handle>());
  }

  @Override
  public void visit(org.jhotdraw.framework.FigureVisitor visitor) {
    visitor.visitFigure(this);
  }

  public boolean dirty() {
    return false;
  }
}
