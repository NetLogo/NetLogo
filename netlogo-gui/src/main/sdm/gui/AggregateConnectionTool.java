// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.ConnectionFigure;
import org.jhotdraw.standard.ConnectionTool;

public strictfp class AggregateConnectionTool
    extends ConnectionTool {

  public AggregateConnectionTool
      (org.jhotdraw.framework.DrawingEditor newDrawingEditor,
       ConnectionFigure newPrototype) {
    super(newDrawingEditor, newPrototype);
  }

  @Override
  public void mouseDown(java.awt.event.MouseEvent e, int x, int y) {
    setAnchorX(x);
    setAnchorY(y);
    setView((org.jhotdraw.framework.DrawingView) e.getSource());

    int ex = e.getX();
    int ey = e.getY();

    setTargetFigure(findConnectionStart(ex, ey, drawing()));
    if (getTargetFigure() != null) {
      setStartConnector(findConnector(ex, ey, getTargetFigure()));
      if (getStartConnector() != null) {
        setConnection(createConnection());
        getConnection().startPoint(ex, ey);
        getConnection().endPoint(ex, ey);
        setAddedFigure(view().add(getConnection()));
      }
    }
  }
}
