package org.nlogo.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;

public strictfp class Utils {

  // this class is not instantiable
  private Utils() { throw new IllegalStateException(); }

  /**
   * Returns the frame containing a component.
   */
  public static Frame getFrame(Component comp) {
    Component top = getTopAncestor(comp);
    if (top instanceof Frame) {
      return (Frame) top;
    } else {
      if (top instanceof Window &&
          top.getParent() != null) {
        return getFrame(top.getParent());
      }
      return null;
    }
  }

  /**
   * Returns the window containing a component.
   */
  public static Window getWindow(Component comp) {
    Component top = getTopAncestor(comp);
    if (top instanceof Window) {
      return (Window) top;
    } else {
      return null;
    }
  }

  public static Component getTopAncestor(Component comp) {
    Component top = comp;
    Container parent = top.getParent();
    while (!(top instanceof Window) && null != parent) {
      top = parent;
      parent = top.getParent();
    }
    return top;
  }

  public static boolean hasAncestorOfClass(Component component, Class<?> theClass) {
    return component != null &&
        (theClass.isInstance(component) ||
            hasAncestorOfClass(component.getParent(), theClass));
  }

  public static Container findAncestorOfClass(Component component, Class<?> theClass) {
    if (component == null) {
      return null;
    }
    if (theClass.isInstance(component)) {
      return (Container) component;
    }
    return findAncestorOfClass(component.getParent(), theClass);
  }

  /**
   * Mixes the rgb components of two colors.
   *
   * @param mix the proportion, from 0 to 1, of the first color in the mix.
   * @return a new color with <code>red = mix*(c1.red) + (1-mix)*c2.red</code>, etc.
   */
  public static Color mixColors(Color c1, Color c2, double mix) {
    mix = StrictMath.min(mix, 1);
    mix = StrictMath.max(mix, 0);
    return
        new Color((int) ((c1.getRed() * mix) + (c2.getRed() * (1 - mix))),
            (int) ((c1.getGreen() * mix) + (c2.getGreen() * (1 - mix))),
            (int) ((c1.getBlue() * mix) + (c2.getBlue() * (1 - mix))));
  }

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

  public static java.awt.event.MouseEvent translateMouseEvent(java.awt.event.MouseEvent e,
                                                              Component target,
                                                              Point offsets) {
    return new java.awt.event.MouseEvent(target, e.getID(), e.getWhen(), e.getModifiers(),
        e.getX() + offsets.x, e.getY() + offsets.y,
        e.getClickCount(), e.isPopupTrigger());
  }

  ///

  public static boolean button1Mask(java.awt.event.MouseEvent e) {
    return (e.getModifiers() & java.awt.event.InputEvent.BUTTON1_MASK) != 0;
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

  /**
   * Converts a java.awt.Color to a 6-digit hex string suitible for HTML/CSS tags. *
   */
  public static String AWTColorToHex(Color c) {
    String s = Integer.toHexString(c.getRGB());
    s = s.substring(s.length() - 6);
    return s;
  }

  /**
   * Wraps a string with HTML font tag for color. *
   */
  public static String colorize(String s, Color c) {
    String str =
        "<font color=\""
            + "#"
            + AWTColorToHex(c)
            + "\">"
            + s
            + "</font>";
    return str;
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
