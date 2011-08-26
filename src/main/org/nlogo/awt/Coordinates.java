package org.nlogo.awt;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.applet.Applet;

public strictfp class Coordinates {

  // this class is not instantiable
  private Coordinates() { throw new IllegalStateException(); }

  /**
   * Converts point from a component's coordinate system to screen coordinates.
   */
  public static void convertPointToScreen(Point p, Component c) {
    int x, y;
    do {
      if (c instanceof Applet || c instanceof Window) {
        Point pp = c.getLocationOnScreen();
        x = pp.x;
        y = pp.y;
      } else {
        x = c.getLocation().x;
        y = c.getLocation().y;
      }
      p.x += x;
      p.y += y;

      if (c instanceof Window || c instanceof Applet) {
        break;
      }
      c = c.getParent();

    } while (c != null);
  }

  /**
   * Converts point to a component's coordinate system from screen coordinates.
   */
  public static void convertPointFromScreen(Point p, Component c) {
    int x, y;
    do {
      if (c instanceof Applet || c instanceof Window) {
        Point pp = c.getLocationOnScreen();
        x = pp.x;
        y = pp.y;
      } else {
        x = c.getLocation().x;
        y = c.getLocation().y;
      }
      p.x -= x;
      p.y -= y;

      if (c instanceof Window || c instanceof Applet) {
        break;
      }
      c = c.getParent();

    } while (c != null);
  }

  /**
   * Returns the location of a component on the screen.
   */
  public static Point getLocationOnScreen(Component c) {
    Point result = new Point(0, 0);
    convertPointToScreen(result, c);
    return result;
  }

  /**
   * Returns the difference between two points.
   */
  public static Point subtractPoints(Point p1, Point p2) {
    return new Point(p1.x - p2.x, p1.y - p2.y);
  }

}
