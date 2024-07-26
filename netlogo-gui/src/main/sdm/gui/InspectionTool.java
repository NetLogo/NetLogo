// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;
import org.nlogo.sdm.Model;
import org.nlogo.sdm.ModelElement;

import org.jhotdraw.framework.FigureEnumeration;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.framework.DrawingView;

import java.util.List;
import java.util.ArrayList;
import scala.collection.JavaConverters;
import java.awt.event.MouseEvent;

class InspectionTool
    extends org.jhotdraw.standard.SelectionTool {

  private final AggregateModelEditor editor;
  private Model model;
  private RateConnectionTool rct;
  private AggregateConnectionTool link;

  InspectionTool(AggregateModelEditor editor, Model model) {
    super(editor);
    this.editor = editor;
    this.model = model;
  }

  protected void inspectFigure(org.jhotdraw.framework.Figure f) {
    editor.inspectFigure(f);
  }

  public void fillInputs(Figure figure, DrawingView view) {
    FigureEnumeration figNum = view.getConnectionFigures(figure);
    if (figNum == null) { return; }

    String currentName="";
    List<String> lst = new ArrayList<String>();
    while (figNum.hasNextFigure()) {
      Figure nFig = figNum.nextFigure();
      if (nFig instanceof BindingConnection) {
        BindingConnection c = ((BindingConnection) nFig);
        if ((c.getEndConnector().owner() == figure)) {
          Figure start = c.getStartConnector().owner();
          if (start instanceof RateConnection) {
            currentName = ((RateConnection) start).nameWrapper();
          }
          else if (start instanceof ConverterFigure) {
            currentName = ((ConverterFigure) start).nameWrapper();
          }
          else if (start instanceof StockFigure) {
            currentName = ((StockFigure) start).nameWrapper();
          }
          if ((currentName != null) && (currentName != "")) {
            lst.add(currentName);
          }
       }
     }
   } // END while
    if (figure instanceof ConverterFigure) {
      ((ConverterFigure) figure).inputs(JavaConverters.collectionAsScalaIterable(lst).toList());
    } else if (figure instanceof RateConnection) {
      ((RateConnection) figure).inputs(JavaConverters.collectionAsScalaIterable(lst).toList());
    }
  }

  @Override
  public void mouseDown(MouseEvent e, int x, int y) {
    org.jhotdraw.framework.DrawingView view = (org.jhotdraw.framework.DrawingView) e.getSource();
    setView(view);

    if (!e.isMetaDown()) {
      if (e.getClickCount() == 2) {
        Figure figure =
            drawing().findFigure(e.getX(), e.getY());

	fillInputs(figure, view);

        if (figure != null) {
          inspectFigure(figure);
          return;
        }
      }
    super.mouseDown(e, x, y);
    } else {
      link = new AggregateConnectionTool(model, editor, new BindingConnection());
      rct = new RateConnectionTool(model, editor, new RateConnection());
      link.mouseDown(e, x, y);
      rct.mouseDown(e, x, y);
    }
  }

  @Override
  public void mouseUp(MouseEvent e, int x, int y) {
    if (!e.isMetaDown()) {
      super.mouseUp(e, x, y);
    }
    else {
      if (link != null) {
        link.mouseUp(e, x, y);
      }

      if (rct != null) {
        rct.mouseUp(e, x, y);
      }
    }
  }

  @Override
  public void mouseDrag(MouseEvent e, int x, int y) {
    if (!e.isMetaDown()) {
      super.mouseDrag(e, x, y);
    }
    else {
      link.mouseDrag(e, x, y);
      rct.mouseDrag(e, x, y);
    }
  }

  @Override
  protected org.jhotdraw.framework.Tool createDragTracker(org.jhotdraw.framework.Figure f) {
    return new AggregateUndoableTool(new org.jhotdraw.standard.DragTracker(editor(), f), editor);
  }
}

