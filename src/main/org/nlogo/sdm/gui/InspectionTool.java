// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

strictfp class InspectionTool
    extends org.jhotdraw.standard.SelectionTool {

  private final AggregateModelEditor editor;

  InspectionTool(AggregateModelEditor editor) {
    super(editor);
    this.editor = editor;
  }

  protected void inspectFigure(org.jhotdraw.framework.Figure f) {
    editor.inspectFigure(f);
  }

  @Override
  public void mouseDown(java.awt.event.MouseEvent e, int x, int y) {
    setView((org.jhotdraw.framework.DrawingView) e.getSource());
    if (e.getClickCount() == 2) {
      org.jhotdraw.framework.Figure figure =
          drawing().findFigure(e.getX(), e.getY());
      if (figure != null) {
        inspectFigure(figure);
        return;
      }
    }
    super.mouseDown(e, x, y);
  }

  @Override
  protected org.jhotdraw.framework.Tool createDragTracker(org.jhotdraw.framework.Figure f) {
    return new AggregateUndoableTool(new org.jhotdraw.standard.DragTracker(editor(), f), editor);
  }
}

