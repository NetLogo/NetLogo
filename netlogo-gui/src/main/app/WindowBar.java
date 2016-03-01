// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

strictfp class WindowBar
    extends javax.swing.JPanel
    // not JComponent otherwise paintComponent() doesn't paint the
    // background color for reasons I can't fathom - ST 8/9/03
{

  enum Type {TOP, BOTTOM, SIDE}

  private final Type type;
  private boolean handles;
  private boolean cornerHandles;
  private final int eastBorder;
  private final int westBorder;

  WindowBar(Type type, int eastBorder, int westBorder, boolean handles) {
    this.type = type;
    this.eastBorder = eastBorder;
    this.westBorder = westBorder;
    this.handles = handles;
    this.cornerHandles = true;
    setBackground(java.awt.Color.GRAY);
    setOpaque(true);
  }

  public void handles(boolean showHandles) {
    handles = showHandles;
  }

  public void cornerHandles(boolean show) {
    cornerHandles = show;
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    g.setColor(getBackground());
    switch (type) {
      case TOP:
        paintTop(g);
        break;
      case BOTTOM:
        paintBottom(g);
        break;
      case SIDE:
        paintSide(g);
        break;
      default:
        throw new IllegalStateException("type = " + type);
    }
  }

  private void paintTop(java.awt.Graphics g) {
    java.awt.Rectangle b = getBounds();
    if (b.width == 0 || b.height == 0) {
      return;
    }

    final int bleed = 5;
    if (westBorder == 0) {
      // draw 3D border on top and right edges only
      drawConvexRect
          (g, new java.awt.Rectangle(-bleed, b.y, b.width + bleed,
              b.height + bleed));
    } else {
      // draw it on the left, too
      drawConvexRect
          (g, new java.awt.Rectangle(0, b.y, b.width, b.height + bleed));
    }

    // now draw 3D border on bottom edge
    // ...westBorder controls whether it extends all the way to our left edge or not
    java.awt.Shape oldClip = g.getClip();
    g.setClip(westBorder - 2, b.height - 3, getWidth() - eastBorder - westBorder + 3, 3);
    drawConvexRect
        (g, new java.awt.Rectangle(-5, 0, b.width + 10, b.height));
    g.setClip(oldClip);

    // now draw handles
    if (handles) {
      g.setColor(java.awt.Color.BLACK);
      if (cornerHandles) {
        g.fillRect(0,
            0,
            westBorder,
            b.height);
      }
      g.fillRect(westBorder + (b.width - eastBorder - westBorder - WidgetWrapper.HANDLE_WIDTH) / 2,
          0,
          WidgetWrapper.HANDLE_WIDTH,
          b.height);
      if (cornerHandles) {
        g.fillRect(b.width - eastBorder,
            0,
            eastBorder,
            b.height);
      }
    }
  }

  private void paintBottom(java.awt.Graphics g) {
    java.awt.Shape oldClip = g.getClip();

    java.awt.Rectangle r = new java.awt.Rectangle(getBounds());
    if (r.width == 0 || r.height == 0) {
      return;
    }
    r.x = 0;
    r.y = -5;
    r.height += 5;


    drawConvexRect(g, r);

    r = new java.awt.Rectangle(getBounds());
    r.x = 0;
    r.y = 0;
    g.setClip(eastBorder - 1, 0, getWidth() - eastBorder - westBorder + 2, 3);

    drawConvexRect(g, r);

    g.setClip(oldClip);

    // now draw handles
    if (handles) {
      java.awt.Rectangle b = getBounds();
      g.setColor(java.awt.Color.BLACK);
      if (cornerHandles) {
        g.fillRect(0,
            0,
            westBorder,
            b.height);
      }
      g.fillRect(westBorder + (b.width - eastBorder - westBorder - WidgetWrapper.HANDLE_WIDTH) / 2,
          0,
          WidgetWrapper.HANDLE_WIDTH,
          b.height);
      if (cornerHandles) {
        g.fillRect(b.width - eastBorder,
            0,
            eastBorder,
            b.height);
      }
    }
  }

  private void paintSide(java.awt.Graphics g) {
    java.awt.Rectangle r = new java.awt.Rectangle(getBounds());
    if (r.width == 0 || r.height == 0) {
      return;
    }
    r.x = 0;
    r.y = -5;
    r.height += 10;
    drawConvexRect(g, r);

    // now draw handles
    if (handles) {
      java.awt.Rectangle b = getBounds();
      g.setColor(java.awt.Color.BLACK);
      g.fillRect(0,
          (b.height - WidgetWrapper.HANDLE_WIDTH) / 2,
          b.width,
          WidgetWrapper.HANDLE_WIDTH);
    }
  }

  private void drawConvexRect(java.awt.Graphics g, java.awt.Rectangle r) {
    g.setColor
        (org.nlogo.awt.Colors.mixColors
            (getForeground(), getBackground(), .5));
    g.drawRect(r.x, r.y, r.width, r.height);
  }

}
