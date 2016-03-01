// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.ConnectionFigure;
import org.jhotdraw.framework.Drawing;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.SingleFigureEnumerator;

import java.awt.Point;
import java.awt.event.MouseEvent;


strictfp class RateConnectionTool
    extends AggregateConnectionTool {
  RateConnectionTool
      (org.jhotdraw.framework.DrawingEditor newDrawingEditor,
       ConnectionFigure newPrototype) {
    super(newDrawingEditor, newPrototype);
  }

  public Figure implyReservoir(int x, int y, Drawing drawing) {
    Figure target = findConnectableFigure(x, y, drawing);
    if (target == null) {
      target = new ReservoirFigure();
      target.displayBox(new Point(x - 15, y - 15),
          new Point(x + 15, y + 15));
      view().add(target);
    }
    if (target.canConnect()
        && ((target instanceof ReservoirFigure)
        || ((target instanceof ModelElementFigure)
        && (((ModelElementFigure) target).getModelElement()
        instanceof org.nlogo.sdm.Stock)))) {
      return target;
    }
    return null;
  }

  @Override
  public Figure findConnectionStart(int x, int y, Drawing drawing) {
    return implyReservoir(x, y, drawing);
  }

  @Override
  public Figure findTarget(int x, int y, Drawing drawing) {
    return super.findTarget(x, y, drawing);
  }

  @Override
  public void mouseUp(MouseEvent e, int x, int y) {
    // If there's no start connecter, then we started with an illegal
    // object.
    if (getStartConnector() == null) {
      return;
    }
    Figure c = findTarget(e.getX(), e.getY(), drawing());

    // If there is no figure to connect to, make a Reservoir,
    // but only if we are not already connected to one.
    if (c == null &&
        !(getStartConnector().owner() instanceof ReservoirFigure)) {
      c = implyReservoir(e.getX(), e.getY(), drawing());
    }

    if (c != null) {
      setEndConnector(findConnector(e.getX(), e.getY(), c));
      if (getEndConnector() != null) {
        connect();
      }
    } else if (getConnection() != null) {
      // remove our original Reservoir
      if (getStartConnector().owner() instanceof ReservoirFigure) {
        view().remove(getStartConnector().owner());
      }

      view().remove(getConnection());
    }
    setConnection(null);
    setStartConnector(null);
    setEndConnector(null);
    setAddedFigure(null);
    editor().toolDone();
  }

  private void connect() {
    // Reservoirs depend on their rate.
    if (getStartConnector().owner() instanceof ReservoirFigure) {
      getConnection().addDependendFigure(getStartConnector().owner());
    }

    if (getEndConnector().owner() instanceof ReservoirFigure) {
      getConnection().addDependendFigure(getEndConnector().owner());
    }

    getConnection().connectStart(getStartConnector());
    getConnection().connectEnd(getEndConnector());
    getConnection().updateConnection();

    setUndoActivity(createUndoActivity());
    getUndoActivity().setAffectedFigures
        (new SingleFigureEnumerator(getAddedFigure()));
  }

}
