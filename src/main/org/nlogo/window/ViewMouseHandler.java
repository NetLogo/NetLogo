// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.AgentException;
import org.nlogo.api.ViewSettings;

public strictfp class ViewMouseHandler
    implements
    java.awt.event.MouseMotionListener,
    java.awt.event.MouseListener {
  private final org.nlogo.api.World world;
  private final ViewSettings settings;
  private final javax.swing.JComponent parent;

  public ViewMouseHandler(javax.swing.JComponent parent, org.nlogo.api.World world, ViewSettings settings) {
    this.world = world;
    this.settings = settings;
    this.parent = parent;
  }

  boolean mouseDown = false;

  public void mouseDown(boolean mouseDown) {
    this.mouseDown = mouseDown;
  }

  public boolean mouseDown() {
    return mouseDown;
  }

  protected double mouseXCor = 0.0;

  public double mouseXCor() {
    return mouseXCor;
  }

  public void mouseXCor(double mouseXCor) {
    this.mouseXCor = mouseXCor;
  }

  protected double mouseYCor = 0.0;

  public double mouseYCor() {
    return mouseYCor;
  }

  public void mouseYCor(double mouseYCor) {
    this.mouseYCor = mouseYCor;
  }

  private boolean mouseInside = false;

  public boolean mouseInside() {
    return mouseInside;
  }

  public void resetMouseCors() {
    mouseXCor = 0;
    mouseYCor = 0;
  }

  private java.awt.Point pt = null;

  public void updateMouseCors() {
    if (mouseInside() && (pt != null)) {
      translatePointToXCorYCor(pt);
    }
  }

  public void mouseClicked(java.awt.event.MouseEvent e) {
  }

  public void mousePressed(java.awt.event.MouseEvent e) {
    if (!e.isPopupTrigger() &&
        org.nlogo.awt.Mouse.hasButton1(e)) {
      mouseDown(true);
    }
  }

  public void mouseReleased(java.awt.event.MouseEvent e) {
    if (!e.isPopupTrigger() &&
        org.nlogo.awt.Mouse.hasButton1(e)) {
      mouseDown(false);
    }
  }

  public void mouseEntered(java.awt.event.MouseEvent e) {
  }

  public void mouseExited(java.awt.event.MouseEvent e) {
    mouseInside = parent.contains(e.getPoint());
  }

  public void mouseDragged(java.awt.event.MouseEvent e) {
    // technically this is redundant, we should already have gotten
    // a mousePressed, but just in case we didn't in some buggy VM,
    // we do this for good measure... - ST 10/5/04
    mouseDown(true);
    // if we press the mouse button inside and then drag outside, we
    // still get mouseDragged events even though the mouse isn't inside
    // us anymore, so unlike in the mouseMoved case, we need this next check
    if (parent.contains(e.getPoint())) {
      updateMouseInside(e.getPoint());
      translatePointToXCorYCor(e.getPoint());
      pt = e.getPoint();
    }
  }

  public void mouseMoved(java.awt.event.MouseEvent e) {
    updateMouseInside(e.getPoint());
    translatePointToXCorYCor(e.getPoint());
    pt = e.getPoint();
  }

  private void updateMouseInside(java.awt.Point p) {
    double x1 = translatePointToUnboundedX(p.x);
    double y1 = translatePointToUnboundedY(p.y);
    try {
      if (world.wrapX(x1) == x1 && world.wrapY(y1) == y1) {
        mouseInside = true;
      } else {
        mouseInside = false;
      }
    } catch (AgentException e) {
      mouseInside = false;
    }
  }

  double translatePointToUnboundedX(int x) {
    java.awt.Rectangle rect = parent.getBounds();
    double xOff = settings.viewOffsetX();
    double dx = ((double) x) / rect.width;
    double xcor = (dx * settings.viewWidth()) + (world.minPxcor() - 0.5);
    xcor += xOff;

    try {
      xcor = world.wrapX(xcor);
    } catch (AgentException e) {
      org.nlogo.util.Exceptions.ignore(e);
    }

    if (settings.patchSize() <= 1.0) {
      xcor = org.nlogo.api.Approximate.approximate(xcor, 0);
    }

    return xcor;
  }

  double translatePointToUnboundedY(int y) {
    java.awt.Rectangle rect = parent.getBounds();

    double yOff = settings.viewOffsetY();
    double dy = ((double) y) / rect.height;
    double ycor = world.maxPycor() + 0.4999999 - (settings.viewHeight() * dy);
    ycor += yOff;

    try {
      ycor = world.wrapY(ycor);
    } catch (AgentException e) {
      org.nlogo.util.Exceptions.ignore(e);
    }

    if (settings.patchSize() <= 1.0) {
      ycor = org.nlogo.api.Approximate.approximate(ycor, 0);
    }

    return ycor;
  }

  void translatePointToXCorYCor(java.awt.Point p) {
    java.awt.Rectangle rect = parent.getBounds();

    int minx = world.minPxcor();
    int maxx = world.maxPxcor();
    int miny = world.minPycor();
    int maxy = world.maxPycor();

    double xOff = settings.viewOffsetX();
    double yOff = settings.viewOffsetY();

    double dx = ((double) p.x) / rect.width;
    double newMouseX = (dx * settings.viewWidth()) + (minx - 0.5);
    newMouseX += xOff;
    try {
      newMouseX = world.wrapX(newMouseX);
    } catch (AgentException e) {
      return;
    }

    if (newMouseX < minx - 0.5) {
      newMouseX = minx - 0.5;
    } else if (newMouseX >= maxx + 0.5) {
      newMouseX = maxx + 0.4999999;
    }

    if (settings.patchSize() <= 1.0) {
      newMouseX = org.nlogo.api.Approximate.approximate(newMouseX, 0);
    }

    double dy = ((double) p.y) / rect.height;
    double newMouseY = maxy + 0.4999999 - (settings.viewHeight() * dy);
    newMouseY += yOff;
    try {
      newMouseY = world.wrapY(newMouseY);
    } catch (AgentException e) {
      return;
    }
    if (newMouseY < miny - 0.5) {
      newMouseY = miny - 0.5;
    } else if (newMouseY >= maxy + 0.5) {
      newMouseY = maxy + 0.4999999;
    }

    if (settings.patchSize() <= 1.0) {
      newMouseY = org.nlogo.api.Approximate.approximate(newMouseY, 0);
    }

    // update mouse coordinates
    mouseXCor(newMouseX);
    mouseYCor(newMouseY);
  }
}
