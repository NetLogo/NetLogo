// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.nlogo.sdm.Model;
import org.jhotdraw.framework.ConnectionFigure;
import org.jhotdraw.framework.DrawingEditor;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.ConnectionTool;

public class AggregateConnectionTool
    extends ConnectionTool {

  private Model model;

  public AggregateConnectionTool
      (Model model,
       DrawingEditor newDrawingEditor,
       ConnectionFigure newPrototype) {
    super(newDrawingEditor, newPrototype);
    this.model = model;
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

  @Override
  public void setAddedFigure(Figure figure) {
    super.setAddedFigure(figure);
    if (figure instanceof ModelElementFigure) {
      this.model.addElement(((ModelElementFigure) figure).getModelElement());
    }
  }
}
