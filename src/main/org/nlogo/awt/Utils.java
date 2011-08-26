package org.nlogo.awt;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public strictfp class Utils {

  // this class is not instantiable
  private Utils() { throw new IllegalStateException(); }

  /**
   * Returns the frame containing a component.
   */
  public static java.awt.Frame getFrame(java.awt.Component comp) {
    java.awt.Component top = getTopAncestor(comp);
    if (top instanceof java.awt.Frame) {
      return (java.awt.Frame) top;
    } else {
      if (top instanceof java.awt.Window &&
          top.getParent() != null) {
        return getFrame(top.getParent());
      }
      return null;
    }
  }

  /**
   * Returns the window containing a component.
   */
  public static java.awt.Window getWindow(java.awt.Component comp) {
    java.awt.Component top = getTopAncestor(comp);
    if (top instanceof java.awt.Window) {
      return (java.awt.Window) top;
    } else {
      return null;
    }
  }

  public static java.awt.Component getTopAncestor(java.awt.Component comp) {
    java.awt.Component top = comp;
    java.awt.Container parent = top.getParent();
    while (!(top instanceof java.awt.Window) && null != parent) {
      top = parent;
      parent = top.getParent();
    }
    return top;
  }

  public static boolean hasAncestorOfClass(java.awt.Component component, Class<?> theClass) {
    return component != null &&
        (theClass.isInstance(component) ||
            hasAncestorOfClass(component.getParent(), theClass));
  }

  public static java.awt.Container findAncestorOfClass(java.awt.Component component, Class<?> theClass) {
    if (component == null) {
      return null;
    }
    if (theClass.isInstance(component)) {
      return (java.awt.Container) component;
    }
    return findAncestorOfClass(component.getParent(), theClass);
  }

  public static boolean loadImage(java.awt.Image image) {
    java.awt.MediaTracker mt =
        new java.awt.MediaTracker(new java.awt.Component() {
        });
    mt.addImage(image, 0);
    try {
      mt.waitForAll();
    } catch (InterruptedException ex) {
      return false;
    }
    return !mt.isErrorAny();
  }

  public static java.awt.Image loadImageResource(String path) {
    java.awt.Image image =
        java.awt.Toolkit.getDefaultToolkit().getImage
            (Utils.class.getResource(path));
    return loadImage(image)
        ? image
        : null;
  }

  public static java.awt.Image loadImageFile(String path, boolean cache) {
    java.awt.Image image;
    if (cache) {
      image = java.awt.Toolkit.getDefaultToolkit().getImage(path);
    } else {
      image = java.awt.Toolkit.getDefaultToolkit().createImage(path);
    }
    return loadImage(image)
        ? image
        : null;
  }

  /**
   * Mixes the rgb components of two colors.
   *
   * @param mix the proportion, from 0 to 1, of the first color in the mix.
   * @return a new color with <code>red = mix*(c1.red) + (1-mix)*c2.red</code>, etc.
   */
  public static java.awt.Color mixColors(java.awt.Color c1, java.awt.Color c2, double mix) {
    mix = StrictMath.min(mix, 1);
    mix = StrictMath.max(mix, 0);
    return
        new java.awt.Color((int) ((c1.getRed() * mix) + (c2.getRed() * (1 - mix))),
            (int) ((c1.getGreen() * mix) + (c2.getGreen() * (1 - mix))),
            (int) ((c1.getBlue() * mix) + (c2.getBlue() * (1 - mix))));
  }

  /**
   * Squeezes a string to fit in a small space.
   */
  public static String shortenStringToFit(String name, int availableWidth, java.awt.FontMetrics metrics) {
    if (metrics.stringWidth(name) > availableWidth) {
      name += "...";
      while (metrics.stringWidth(name) > availableWidth && name.length() > 3) {
        name = name.substring(0, name.length() - 4) + "...";
      }
    }
    return name;
  }

  public static java.awt.event.MouseEvent translateMouseEvent(java.awt.event.MouseEvent e,
                                                              java.awt.Component target,
                                                              java.awt.Point offsets) {
    return new java.awt.event.MouseEvent(target, e.getID(), e.getWhen(), e.getModifiers(),
        e.getX() + offsets.x, e.getY() + offsets.y,
        e.getClickCount(), e.isPopupTrigger());
  }

  /// line wrapping

  public static List<String> breakLines(String text,
                                        java.awt.FontMetrics metrics,
                                        int width) {
    List<String> result = new ArrayList<String>();
    while (text.length() > 0) {
      int index = 0;
      while (index < text.length()
          && (metrics.stringWidth(text.substring(0, index + 1)) < width
          || text.charAt(index) == ' ')) {
        if (text.charAt(index) == '\n') {
          text = text.substring(0, index) + ' ' + text.substring(index + 1);
          index++;
          break;
        }
        index++;
      }

      // if index is still 0, then this line will never wrap
      // so just give up and return the whole thing as one line
      if (index == 0) {
        result.add(text);
        return result;
      }

      // invariant: index is now the index of the first non-space
      // character which won't fit in the current line

      if (index < text.length()) {
        int spaceIndex = text.substring(0, index).lastIndexOf(' ');
        if (spaceIndex >= 0) {
          index = spaceIndex + 1;
        }
      }

      // invariant: index is now the index of the first character
      // which will *not* be included in the current line

      String thisLine = text.substring(0, index);
      if (index < text.length()) {
        text = text.substring(index, text.length());
      } else {
        text = "";
      }
      result.add(thisLine);
    }
    if (result.isEmpty()) {
      result.add("");
    }
    return result;
  }

  /// for centering frames and dialogs

  public static void center(java.awt.Window window, java.awt.Window parent) {
    int x, y;
    java.awt.Rectangle availBounds;
    if (parent == null) {
      java.awt.Point center =
          java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
              .getCenterPoint();
      x = center.x - (window.getWidth() / 2);
      y = center.y - (window.getHeight() / 2);
      availBounds = java.awt.GraphicsEnvironment
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
  public static void moveNextTo(java.awt.Component c1, java.awt.Component c2) {
    final int SPACE = 4;
    int right = c2.getBounds().x + c2.getBounds().width + SPACE;
    int below = c2.getBounds().y + c2.getBounds().height + SPACE;
    int left = c2.getBounds().x - c1.getBounds().width - SPACE;

    java.awt.Rectangle screenBounds = c2.getGraphicsConfiguration().getBounds();

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

  /// clipboard

  public static String getClipboardAsString(Object requester) {
    java.awt.Toolkit kit = java.awt.Toolkit.getDefaultToolkit();
    java.awt.datatransfer.Clipboard clipboard = kit.getSystemClipboard();
    java.awt.datatransfer.Transferable transferable = clipboard.getContents(requester);
    if (transferable == null) {
      return null;
    }
    try {
      return (String) transferable.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
    } catch (java.io.IOException ex) {
      // ignore exception
      return null;
    } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
      // ignore exception
      return null;
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

  public static java.awt.image.BufferedImage paintToImage(java.awt.Component comp) {
    java.awt.image.BufferedImage image =
        new java.awt.image.BufferedImage
            (comp.getWidth(), comp.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
    // If we just call paint() here we get weird results on
    // windows printAll appears to work ev 5/13/09
    comp.printAll(image.getGraphics());
    return image;
  }

  public static void addNoisyFocusListener(final java.awt.Component comp) {
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
  public static String AWTColorToHex(java.awt.Color c) {
    String s = Integer.toHexString(c.getRGB());
    s = s.substring(s.length() - 6);
    return s;
  }

  /**
   * Wraps a string with HTML font tag for color. *
   */
  public static String colorize(String s, java.awt.Color c) {
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
  public static void drawStringInBox(java.awt.Graphics g, String string, int x, int y) {
    java.awt.Color color = g.getColor();
    java.awt.FontMetrics metrics = g.getFontMetrics();
    y -= metrics.getAscent();
    int width = metrics.stringWidth(string);
    int height = metrics.getHeight();
    g.setColor(java.awt.Color.WHITE);
    g.fillRect(x, y, width + 6, height + 6);
    g.setColor(color);
    g.drawRect(x, y, width + 6, height + 6);
    g.drawString(string, x + 3, y + 3 + metrics.getAscent());
  }

}
