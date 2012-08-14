// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

strictfp class AggregateUndoableTool
    extends org.jhotdraw.util.UndoableTool
    implements org.nlogo.window.Event.LinkChild {
  private final java.awt.Component linkParent;

  AggregateUndoableTool(org.jhotdraw.framework.Tool newWrappedTool, java.awt.Component linkParent) {
    super(newWrappedTool);
    this.linkParent = linkParent;
  }

  public Object getLinkParent() {
    return linkParent;
  }

  @Override
  public void mouseDrag(java.awt.event.MouseEvent e, int x, int y) {
    super.mouseDrag(e, x, y);
    new org.nlogo.window.Events.DirtyEvent().raise(this);
  }
}
