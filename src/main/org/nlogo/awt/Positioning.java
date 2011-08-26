package org.nlogo.awt;

import java.awt.Window;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.GraphicsEnvironment;

public final strictfp class Positioning {

  // this class is not instantiable
  private Positioning() { throw new IllegalStateException(); }

  /// for centering frames and dialogs

  public static void center(Window window, Window parent) {
    int x, y;
    Rectangle availBounds;
    if (parent == null) {
      Point center =
          GraphicsEnvironment.getLocalGraphicsEnvironment()
              .getCenterPoint();
      x = center.x - (window.getWidth() / 2);
      y = center.y - (window.getHeight() / 2);
      availBounds = GraphicsEnvironment
          .getLocalGraphicsEnvironment()
          .getMaximumWindowBounds();
    } else {
      x = parent.getLocation().x + parent.getWidth() / 2 -
          window.getPreferredSize().width / 2;
      y = parent.getLocation().y + parent.getHeight() / 2 -
          window.getPreferredSize().height / 2;
      availBounds = parent.getGraphicsConfiguration().getBounds();
    }
    x = StrictMath.min(x, availBounds.x + availBounds.width - window.getWidth());
    y = StrictMath.min(y, availBounds.y + availBounds.height - window.getHeight());
    if (x < 0) {
      x = 0;
    }
    if (y < 0) {
      y = 0;
    }
    window.setLocation(x, y);
  }

  /**
   * Moves c1 next to c2. Usually on it's right, but if there
   * isn't enough room tries to the left or below.
   */
  public static void moveNextTo(Component c1, Component c2) {
    final int SPACE = 4;
    int right = c2.getBounds().x + c2.getBounds().width + SPACE;
    int below = c2.getBounds().y + c2.getBounds().height + SPACE;
    int left = c2.getBounds().x - c1.getBounds().width - SPACE;

    Rectangle screenBounds = c2.getGraphicsConfiguration().getBounds();

    if (screenBounds.width - right - c1.getBounds().width > 0) {
      c1.setLocation(right, c2.getLocation().y);
    } else if (left > screenBounds.x) {
      c1.setLocation(left, c2.getLocation().y);
    } else if (screenBounds.height - below - c1.getBounds().height > 0) {
      c1.setLocation(c2.getLocation().x, below);
    } else {
      c1.setLocation((screenBounds.x + screenBounds.width)
          - c1.getBounds().width, c2.getLocation().y);
    }
  }

}
