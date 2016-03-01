// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.framework.Figure;
import org.jhotdraw.standard.StandardDrawingView;

strictfp class AggregateDrawingView
    extends StandardDrawingView {
  AggregateDrawingView(AggregateModelEditor editor) {
    super(editor);
    setBackground(java.awt.Color.WHITE);
  }

  AggregateDrawingView(AggregateModelEditor editor, int width, int height) {
    super(editor, width, height);
    setBackground(java.awt.Color.WHITE);
  }

  @Override
  public Figure add(Figure figure) {
    if (figure instanceof ModelElementFigure
        && ((ModelElementFigure) figure).getModelElement() != null) {
      ModelElementFigure fig = ((ModelElementFigure) figure);
      ((AggregateDrawing) drawing()).getModel().addElement(fig.getModelElement());
    }
    figure = super.add(figure);

    // I had tried to do this to control the z-ordering of the
    // items on screen, but it didn't work... not sure why...
    // - ST 12/14/04, 10/11/05
    //      if( figure instanceof ConnectorConnection ||
    //          figure instanceof BindingConnection )
    //      {
    //          drawing().sendToLayer( figure , -1 ) ;
    //      }
    //      else
    //      {
    //          drawing().sendToLayer( figure , Integer.MAX_VALUE ) ;
    //      }

    return figure;
  }
}
