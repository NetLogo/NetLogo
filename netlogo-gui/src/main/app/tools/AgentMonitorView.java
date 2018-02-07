// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools;

// leaving this in Java for now since our superclass is in Java - ST 8/13/10

import org.nlogo.agent.Agent;
import org.nlogo.agent.Link;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.window.GUIWorkspaceScala;

public strictfp class AgentMonitorView
    extends org.nlogo.window.View {
  public AgentMonitorView(GUIWorkspaceScala workspace) {
    super(workspace);
    radius = (workspace.world().worldWidth() - 1) / 2;
    addMouseListener(popupListener);
  }

  private Perspective perspective = PerspectiveJ.create(PerspectiveJ.OBSERVE);

  @Override
  public boolean isDead() {
    return (agent != null && agent.id() == -1);
  }

  private Agent agent;

  public Agent agent() {
    return agent;
  }

  // NOTE: This sets a follow perspective without specifying a follow distance
  // This is because the follow distance is tracked separately in this class
  // through radius, viewWidth, and viewHeight. It would be great if in the future
  // this used the followDistance of the perspective, but I expect it would
  // be fairly difficult to get there. RG 2/16
  public void agent(Agent agent) {
    this.agent = agent;
    perspective = PerspectiveJ.create(PerspectiveJ.FOLLOW, agent);
  }

  private double radius;

  public double radius() {
    return radius;
  }

  public void radius(double radius) {
    this.radius = radius;
    patchSize((viewWidth() * _patchSize()) / ((radius * 2) + 1));
    _viewWidth_$eq((radius * 2) + 1);
    _viewHeight_$eq((radius * 2) + 1);
    incrementalUpdateFromEventThread();
  }

  public void patchSize(double patchSize) {
    java.awt.Font font = getFont();
    int newFontSize = StrictMath.max(1, (int) (patchSize * fontSizeRatio));
    setFont(new java.awt.Font(font.getName(), font.getStyle(), newFontSize));
    _patchSize_$eq(patchSize);
  }

  @Override
  public Perspective perspective() {
    return perspective;
  }

  @Override
  public double viewOffsetX() {
    if (perspective.kind() == PerspectiveJ.OBSERVE || agent instanceof org.nlogo.agent.DummyLink) {
      return 0;
    }

    double x = 0;
    if (agent instanceof Turtle) {
      x = ((Turtle) agent).xcor();
    } else if (agent instanceof Patch) {
      x = ((Patch) agent).pxcor;
    } else {
      x = ((Link) agent).midpointX();
    }
    return x - radius - workspace().world().minPxcor();
  }

  @Override
  public double viewOffsetY() {
    if (perspective.kind() == PerspectiveJ.OBSERVE || agent instanceof org.nlogo.agent.DummyLink) {
      return 0;
    }

    double y = 0;
    if (agent instanceof Turtle) {
      y = ((Turtle) agent).ycor();
    } else if (agent instanceof Patch) {
      y = ((Patch) agent).pycor;
    } else {
      y = ((Link) agent).midpointY();
    }

    return y + ((viewHeight() - 1) / 2) - workspace().world().maxPycor();
  }

  @Override
  public double patchSize() {
    return _patchSize();
  }

  private double fontSizeRatio;

  @Override
  public void applyNewFontSize(int newFontSize, int zoom) {
    fontSizeRatio = newFontSize / workspace().world().patchSize();
    super.applyNewFontSize(newFontSize, zoom);
  }

  @Override
  public void setSize(int worldWidth, int worldHeight, double patchSize) {
    patchSize(patchSize);
    _viewWidth_$eq(worldWidth);
    _viewHeight_$eq(worldHeight);
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    patchSize(width / (radius * 2 + 1));
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    patchSize(width / (radius * 2 + 1));
  }

  @Override
  public boolean drawSpotlight() {
    return false;
  }

  private final java.awt.event.MouseListener popupListener =
      new java.awt.event.MouseAdapter() {
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
          if (e.isPopupTrigger()) {
            doPopup(e);
          }
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
          if (e.isPopupTrigger()) {
            doPopup(e);
          }
        }
      };

  public void close() {
    workspace().viewManager().remove(this);
  }

  private void doPopup(java.awt.event.MouseEvent e) {
    javax.swing.JPopupMenu menu = new org.nlogo.swing.WrappingPopupMenu();
    java.awt.Point p = e.getPoint();
    p = populateContextMenu(menu, p, (java.awt.Component) e.getSource());
    if (menu.getSubElements().length > 0) {
      menu.show((java.awt.Component) e.getSource(), p.x, p.y);
    }
    e.consume();
  }
}
