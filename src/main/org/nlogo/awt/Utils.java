package org.nlogo.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

public strictfp class Utils {

  // this class is not instantiable
  private Utils() { throw new IllegalStateException(); }

  /**
   * Squeezes a string to fit in a small space.
   */
  public static String shortenStringToFit(String name, int availableWidth, FontMetrics metrics) {
    if (metrics.stringWidth(name) > availableWidth) {
      name += "...";
      while (metrics.stringWidth(name) > availableWidth && name.length() > 3) {
        name = name.substring(0, name.length() - 4) + "...";
      }
    }
    return name;
  }

  /// event thread stuff

  public static void mustBeEventDispatchThread() {
    if (!java.awt.EventQueue.isDispatchThread()) {
      throw new IllegalStateException("not event thread: " + Thread.currentThread());
    }
  }

  public static void cantBeEventDispatchThread() {
    if (java.awt.EventQueue.isDispatchThread()) {
      throw new IllegalStateException();
    }
  }

  /// thread safety utils

  // At the moment this one is useless, but historically we sometimes
  // had extra stuff attached here, and we might want to add some
  // again in the future, so... - ST 8/3/03
  public static void invokeLater(final Runnable r) {
    java.awt.EventQueue.invokeLater(r);
  }

  public static void invokeAndWait(final Runnable r)
      throws InterruptedException {
    try {
      java.awt.EventQueue.invokeAndWait(r);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      throw new IllegalStateException(ex);
    }
  }

  ///

  public static void addNoisyFocusListener(final Component comp) {
    comp.addFocusListener
        (new java.awt.event.FocusListener() {
          public void focusGained(java.awt.event.FocusEvent fe) {
            System.out.println(comp + " gained focus at " + System.nanoTime());
            System.out.println("oppositeComponent = " + fe.getOppositeComponent());
          }

          public void focusLost(java.awt.event.FocusEvent fe) {
            System.out.println(comp + " lost focus at " + System.nanoTime());
            System.out.println("oppositeComponent = " + fe.getOppositeComponent());
          }
        });
  }

  // used by System Dynamics Modeler
  public static void drawStringInBox(Graphics g, String string, int x, int y) {
    Color color = g.getColor();
    FontMetrics metrics = g.getFontMetrics();
    y -= metrics.getAscent();
    int width = metrics.stringWidth(string);
    int height = metrics.getHeight();
    g.setColor(Color.WHITE);
    g.fillRect(x, y, width + 6, height + 6);
    g.setColor(color);
    g.drawRect(x, y, width + 6, height + 6);
    g.drawString(string, x + 3, y + 3 + metrics.getAscent());
  }

}
