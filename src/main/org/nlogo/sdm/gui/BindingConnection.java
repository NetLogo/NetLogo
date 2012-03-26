// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.figures.LineConnection;
import org.jhotdraw.framework.Figure;
import org.nlogo.sdm.Binding;
import org.nlogo.sdm.ModelElement;
import org.nlogo.sdm.Reservoir;

public strictfp class BindingConnection extends LineConnection {
  public BindingConnection() {
    setEndDecoration(null);
    setStartDecoration(null);
  }

  @Override
  public boolean canConnect(Figure start, Figure end) {
    ModelElement startElement =
        ((ModelElementFigure) start).getModelElement();
    ModelElement endElement =
        ((ModelElementFigure) end).getModelElement();

    // can't bind to ourselves
    if (start == end) {
      return false;
    }
    return (((startElement instanceof Binding.Source)
        && !(startElement instanceof Reservoir))
        && (endElement instanceof Binding.Target));
  }

  @Override
  public boolean canConnect() {
    return false;
  }

  @Override
  public void handleConnect(Figure start, Figure end) {
    end.connectorVisibility(true, this);
  }

  @Override
  protected java.awt.Rectangle invalidateRectangle(java.awt.Rectangle r) {
    java.awt.Rectangle box = super.invalidateRectangle(r);
    box.grow(50, 50);
    return box;
  }

  @Override
  public void drawLine(java.awt.Graphics g,
                       int x1,
                       int y1,
                       int x2,
                       int y2) {
    ((java.awt.Graphics2D) g).setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(java.awt.Color.BLACK);

    // if height is different, just draw a line
    if (StrictMath.abs(y1 - y2) > 10 && StrictMath.abs(x1 - x2) > 10) {
      g.drawLine(x1, y1, x2, y2);
    } else {
      int arcx = x1;
      int arcy = y1;
      if (x2 < x1) {
        arcx = x2;
      }
      if (y2 < y1) {
        arcy = y2;
      }

      int height = StrictMath.abs(y1 - y2);
      int width = StrictMath.abs(x1 - x2);
      int startangle = 0;
      int arcangle = 0;
      int xadjust = 0;
      int yadjust = 0;

      if (height <= 10) {
        yadjust = 20;
        startangle = 180;
        arcangle = -180;
        height = 40;
        x1 = x2 - ((x2 - x1) / 8);
        y1 = arcy - (height);
        y2 = arcy;
      } else {
        xadjust = 20;
        startangle = 90;
        arcangle = 180;
        width = 40;
        y1 = y2 - ((y2 - y1) / 8);
        x1 = arcx - (width);
        x2 = arcx;
      }

      g.drawArc
          (arcx - xadjust, arcy - yadjust,
              width, height, startangle, arcangle);
    }

    double angle = StrictMath.atan2((x2 - x1), (y2 - y1));
    double turn = StrictMath.PI / 4;
    double c = 11d;

    int xdiff1 = (int) StrictMath.round(c * StrictMath.cos(angle + turn));
    int ydiff1 = (int) StrictMath.round(c * StrictMath.sin(angle + turn));
    int xdiff2 = (int) StrictMath.round(c * StrictMath.cos(angle - turn));
    int ydiff2 = (int) StrictMath.round(c * StrictMath.sin(angle - turn));

    int[] xArray = new int[3];
    int[] yArray = new int[3];
    xArray[0] = x2;
    yArray[0] = y2;
    xArray[1] = x2 + xdiff1;
    yArray[1] = y2 - ydiff1;
    xArray[2] = x2 - xdiff2;
    yArray[2] = y2 + ydiff2;
    g.fillPolygon(xArray, yArray, 3);
  }
}
