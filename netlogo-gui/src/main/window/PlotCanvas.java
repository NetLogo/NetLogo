// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.plot.Plot;
import org.nlogo.plot.PlotPainter;

strictfp class PlotCanvas extends javax.swing.JPanel {

  final Plot plot;
  private final PlotPainter painter;
  public boolean dirty = true;

  void repaintIfNeeded() {
    if (dirty) {
      painter.setupOffscreenImage(getWidth(), getHeight());
      painter.refresh();
      dirty = false;
      repaint();
    }
  }

  public void makeDirty() {
    dirty = true;
  }

  public boolean isDirty() {
    return dirty;
  }

  PlotCanvas(Plot plot) {
    this.plot = plot;
    painter = new PlotPainter(plot);
    setOpaque(true);
    setBackground(java.awt.Color.WHITE);
    setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseMotionListener);
  }

  /// mouse handling

  private boolean mouseHere = false;
  private java.awt.Point mouseLoc = null;

  private final java.awt.event.MouseListener mouseListener =
      new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
          mouseHere = true;
          mouseLoc = e.getPoint();
          repaint();
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
          mouseHere = false;
          repaint();
        }
      };
  private final java.awt.event.MouseMotionListener mouseMotionListener =
      new java.awt.event.MouseMotionListener() {
        public void mouseDragged(java.awt.event.MouseEvent e) {
          mouseLoc = e.getPoint();
          mouseHere =
              mouseLoc.x >= 0 && mouseLoc.y >= 0 &&
                  mouseLoc.x < getBounds().width && mouseLoc.y < getBounds().height;
          repaint();
        }

        public void mouseMoved(java.awt.event.MouseEvent e) {
          mouseHere = true;  // shouldn't be necessary, but Linux and OS X VM's are flaky
          // (note: comment may out of date for Java 1.4 - ST 8/9/03)
          mouseLoc = e.getPoint();
          repaint();
        }
      };

  /// other methods

  @Override
  public void paintComponent(java.awt.Graphics g) {
    painter.setupOffscreenImage(getWidth(), getHeight());
    painter.drawImage(g);
    if (mouseHere) {
      drawMouseCoords(g);
    }
  }

  private void drawMouseCoords(java.awt.Graphics g) {
    java.awt.FontMetrics fontMetrics = getFontMetrics(getFont());
    int fontHeight = fontMetrics.getAscent();

    // prepare xcor
    int xPrecision = (int) -StrictMath.floor(StrictMath.log(fromScreenXCor(1) - fromScreenXCor(0)) / StrictMath.log(10));
    String xcor =
        Double.toString(org.nlogo.api.Approximate.approximate(fromScreenXCor(mouseLoc.x), xPrecision));
    if (xPrecision <= 0) {
      // strip the trailing ".0"
      xcor = xcor.substring(0, xcor.length() - 2);
    }
    int xcorWidth = fontMetrics.stringWidth(xcor);

    // prepare ycor
    int yPrecision = (int) -StrictMath.floor(StrictMath.log(fromScreenYCor(0) - fromScreenYCor(1)) / StrictMath.log(10));
    String ycor =
        Double.toString(org.nlogo.api.Approximate.approximate(fromScreenYCor(mouseLoc.y), yPrecision));
    if (yPrecision <= 0) {
      // strip the trailing ".0"
      ycor = ycor.substring(0, ycor.length() - 2);
    }
    int ycorWidth = fontMetrics.stringWidth(ycor);

    // position & draw xcor
    int xx = mouseLoc.x - xcorWidth / 2;
    xx = StrictMath.max(xx, ycorWidth);
    xx = StrictMath.min(xx, getBounds().width - xcorWidth);
    int xy = getBounds().height - fontHeight + 1;
    g.setColor(InterfaceColors.PLOT_BACKGROUND);
    g.fillRect(xx - 1, xy,
        xcorWidth + 1, fontHeight + 1);
    g.setColor(java.awt.Color.BLACK);
    g.drawString(xcor, xx, xy + fontHeight - 1);

    // position & draw ycor
    int yy = mouseLoc.y - fontHeight / 2;
    yy = StrictMath.max(yy, 0);
    yy = StrictMath.min(yy, getBounds().height - 2 * fontHeight);
    int yx = 0;
    g.setColor(InterfaceColors.PLOT_BACKGROUND);
    g.fillRect(yx, yy, ycorWidth + 1, fontHeight + 1);
    g.setColor(java.awt.Color.BLACK);
    g.drawString(ycor, yx, yy + fontHeight - 1);
  }

  private double fromScreenXCor(double screenX) {
    double width = getWidth() - 1;
    double range = plot.xMax() - plot.xMin();
    return plot.xMin() + screenX * range / width;
  }

  private double fromScreenYCor(double screenY) {
    double height = getHeight() - 1;
    double range = plot.yMax() - plot.yMin();
    return plot.yMax() - screenY * range / height;
  }

}
