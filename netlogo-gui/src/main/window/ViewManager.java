// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.ViewInterface;

public strictfp class ViewManager {
  private final java.util.LinkedList<ViewInterface> views = new java.util.LinkedList<ViewInterface>();

  // note that primary views *must* be local views aka not hubnet
  public LocalViewInterface getPrimary() {
    return (LocalViewInterface) views.get(0);
  }

  public void setPrimary(LocalViewInterface view) {
    views.remove(view);
    views.addFirst(view);
  }

  public void setSecondary(ViewInterface view) {
    views.remove(view);
    views.add(1, view);
  }

  public void add(ViewInterface v) {
    views.addLast(v);
  }

  public void remove(ViewInterface v) {
    views.remove(v);
  }

  void paintImmediately(boolean force) {
    for (ViewInterface view : views) {
      view.paintImmediately(force);
    }
  }

  void framesSkipped() {
    for (ViewInterface v : views) {
      v.framesSkipped();
    }
  }

  void incrementalUpdateFromEventThread() {
    for (ViewInterface v : views) {
      if (!v.isDead() && v.viewIsVisible()) {
        v.incrementalUpdateFromEventThread();
      }
    }
  }

  private final Runnable updateRunnable =
      new Runnable() {
        public void run() {
          incrementalUpdateFromEventThread();
        }
      };

  void incrementalUpdateFromJobThread() {
    try {
      org.nlogo.awt.EventQueue.invokeAndWait(updateRunnable);
    } catch (InterruptedException ex) {
      getPrimary().repaint();
    }
  }

  void applyNewFontSize(int newFontSize) {
    for (ViewInterface v : views) {
      v.applyNewFontSize(newFontSize, 0);
    }
  }

  public void shapeChanged(org.nlogo.core.Shape shape) {
    for (ViewInterface view : views) {
      view.shapeChanged(shape);
    }
  }

  public boolean mouseDown() {
    for (ViewInterface view : views) {
      if (view.mouseDown()) {
        return true;
      }
    }
    return false;
  }

  public boolean mouseInside() {
    for (ViewInterface view : views) {
      if (view.mouseInside()) {
        return true;
      }
    }
    return false;
  }

  public double mouseXCor() {
    for (ViewInterface view : views) {
      if (view.mouseInside()) {
        return view.mouseXCor();
      }
    }
    return getPrimary().mouseXCor();
  }

  public double mouseYCor() {
    for (ViewInterface view : views) {
      if (view.mouseInside()) {
        return view.mouseYCor();
      }
    }
    return getPrimary().mouseYCor();
  }

  public void resetMouseCors() {
    for (ViewInterface view : views) {
      view.resetMouseCors();
    }
  }
}
