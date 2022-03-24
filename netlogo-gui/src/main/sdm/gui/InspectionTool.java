// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;
import org.nlogo.sdm.Model;

strictfp class InspectionTool
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

  @Override
  public void mouseDown(java.awt.event.MouseEvent e, int x, int y) {
    setView((org.jhotdraw.framework.DrawingView) e.getSource());

    if (! e.isMetaDown()){
      if (e.getClickCount() == 2) {
        org.jhotdraw.framework.Figure figure =
            drawing().findFigure(e.getX(), e.getY());
        if (figure != null) {
          inspectFigure(figure);
          return;
        }
      }
    super.mouseDown(e, x, y);
    } else {
      link= new AggregateConnectionTool(model, editor, new BindingConnection());
      rct = new RateConnectionTool(model, editor, new RateConnection());
      link.mouseDown(e, x, y);
      rct.mouseDown(e, x, y);
    }
  }

  @Override
  public void mouseUp(java.awt.event.MouseEvent e, int x, int y) {
    if (! e.isMetaDown()){
      super.mouseUp(e,x,y);
    }
    else {
      link.mouseUp(e, x, y);
      rct.mouseUp(e, x, y);
    }
  }

  @Override
  public void mouseDrag(java.awt.event.MouseEvent e, int x, int y) {
    if (! e.isMetaDown()){
      super.mouseDrag(e,x,y);
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

