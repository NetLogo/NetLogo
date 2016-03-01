// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui;

import org.jhotdraw.figures.LineConnection;
import org.jhotdraw.framework.Connector;
import org.jhotdraw.framework.Figure;
import org.jhotdraw.util.Geom;
import org.nlogo.api.Property;
import org.nlogo.sdm.ModelElement;
import org.nlogo.sdm.Reservoir;
import org.nlogo.sdm.Stock;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

public strictfp class RateConnection
    extends LineConnection
    implements ModelElementFigure,
    org.nlogo.api.Editable {
  static final int ICON_SIZE = 25;

  private org.nlogo.sdm.Rate rate;
  private String name;

  public RateConnection() {
    setEndDecoration(null);
    setStartDecoration(null);
    rate = new org.nlogo.sdm.Rate();
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String name) {
    this.name = name;
  }


  public org.nlogo.sdm.ModelElement getModelElement() {
    return rate;
  }

  // RateConnections never have errors
  public boolean anyErrors() {
    return false;
  }

  public void error(Object o, Exception e) {
  }

  public Exception error(Object key) {
    return null;
  }

  public int sourceOffset() {
    return 0;
  }

  @Override
  public boolean containsPoint(int x, int y) {
    Rectangle bounds = displayBox();
    bounds.grow(4, 4);
    if (!bounds.contains(x, y)) {
      return false;
    }
    Point middle = middlePoint();
    return ICON_SIZE / 2 >= Geom.length(x, y, middle.x, middle.y);
  }

  @Override
  public boolean canConnect(Figure start, Figure end) {
    ModelElement source = ((ModelElementFigure) start).getModelElement();
    ModelElement sink = ((ModelElementFigure) end).getModelElement();

    if (sink == source) {
      return false;
    }
    if (!(source instanceof Stock)) {
      return false;
    }
    if (!(sink instanceof Stock)) {
      return false;
    }
    if ((source instanceof Reservoir) && sink instanceof Reservoir) {
      return false;
    }

    return true;
  }

  @Override
  public void handleConnect(Figure start, Figure end) {
    rate.setSource((org.nlogo.sdm.Stock)
        ((ModelElementFigure) start).getModelElement());
    rate.setSink((org.nlogo.sdm.Stock)
        ((ModelElementFigure) end).getModelElement());
    // whenever we reconnect to new source or sink, we reset the
    // middle point.  It can still be hand edited of course -- CLB
    initializeMiddlePoint();
  }


  @Override
  public boolean canConnect() {
    // even though this is a connection itself, it can be connected
    return true;
  }

  @Override
  public Connector connectorAt(int x, int y) {
    return new ChopRateConnector(this);
  }

  @Override
  public java.awt.Rectangle displayBox() {
    // increase displayBox size to include entire icon
    java.awt.Rectangle r = super.displayBox();
    r.grow(30, 50);
    return r;
  }

  @Override
  public java.awt.Insets connectionInsets() {
    return new java.awt.Insets(0, 0, 0, 0);
  }

  @Override
  public void draw(java.awt.Graphics g) {
    ((java.awt.Graphics2D) g).setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    super.draw(g);
    if (getModelElement() != null) {

      g.setColor(java.awt.Color.BLACK);
      java.awt.Color oldColor = g.getColor();
      if (!getModelElement().isComplete()) {
        g.setColor(java.awt.Color.RED);
      }
      String displayname = getModelElement().getName();
      if (displayname.length() == 0) {
        displayname = "?";
      }

      Point center = middlePoint();
      int labelX = center.x - ICON_SIZE - 3;
      int labelY = center.y - ICON_SIZE + 50;
      java.awt.Font oldFont = g.getFont();
      g.setFont(oldFont.deriveFont(java.awt.Font.ITALIC, 10.0f));
      Utils.drawStringInBox(g, displayname,
          labelX, labelY);
      g.setFont(oldFont);
      g.setColor(oldColor);
    }
  }

  @Override
  public void drawLine(java.awt.Graphics g,
                       int x1,    // location of source
                       int y1,
                       int x2,    // location of sink
                       int y2) {
    ((java.awt.Graphics2D) g).setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    // go over then up/down
    int mx = x1 + ((x2 - x1)) / 2; // where the line breaks
    int my = y1 + ((y2 - y1)) / 2; // where the line breaks
    int wx = 4; // horizontal width of line
    int wy = 4; // vertical width of line
    int a = 6; // arrow height
    int aw = 8; // arrow width
    int[] xcors;
    int[] ycors;
    if (x2 < x1) {
      aw = -aw;
      wx = -wx;
    }
    if (y2 < y1) {
      wy = -wy;
      a = -a;
    }
    if (StrictMath.abs(x2 - x1) < StrictMath.abs(y2 - y1)) {
      if (y1 > y2) {
        a = -a;
        aw = -aw;
      }
      if (x1 > x2) {
        aw = -aw;
        a = -a;
      }
      xcors = new int[]{x1 - wx, x1 - wx, x2 - wx, x2 - wx, x2 - wx - a, x2, x2 + wx + a, x2 + wx, x2 + wx, x1 + wx, x1 + wx};
      ycors = new int[]{y1, my + wy, my + wy, y2 - aw, y2 - aw, y2 + aw, y2 - aw, y2 - aw, my - wy, my - wy, y1};
    } else {
      xcors = new int[]{x1, mx + wx, mx + wx, x2 - aw, x2 - aw, x2 + aw, x2 - aw, x2 - aw, mx - wx, mx - wx, x1};
      ycors = new int[]{y1 - wy, y1 - wy, y2 - wy, y2 - wy, y2 - wy - a, y2, y2 + wy + a, y2 + wy, y2 + wy, y1 + wy, y1 + wy};
    }
    g.setColor(java.awt.Color.LIGHT_GRAY);
    g.fillPolygon(xcors, ycors, 11);
    g.setColor(java.awt.Color.BLACK);
    g.drawPolyline(xcors, ycors, 11);

    drawIcon(g);
  }

  private void drawIcon(java.awt.Graphics g) {
    // sink icon halfway through first line
    Point middle = middlePoint();
    int cx = middle.x;
    int cy = middle.y;
    int cs = ICON_SIZE;

    g.setColor(java.awt.Color.GRAY);
    g.fillPolygon
        (new int[]{cx - 1, cx + 1, cx + 1, cx + (cs / 2) - 2, cx + (cs / 2) - 4, cx - (cs / 2) + 4, cx - (cs / 2) + 2, cx - 1, cx - 1},
            new int[]{cy - 14, cy - 14, cy - cs, cy - cs, cy - (cs + 2), cy - (cs + 2), cy - cs, cy - cs, cy - 14},
            9);
    g.setColor(java.awt.Color.BLACK);
    g.drawPolygon
        (new int[]{cx - 1, cx + 1, cx + 1, cx + (cs / 2) - 2, cx + (cs / 2) - 4, cx - (cs / 2) + 4, cx - (cs / 2) + 2, cx - 1, cx - 1},
            new int[]{cy - 14, cy - 14, cy - cs, cy - cs, cy - (cs + 2), cy - (cs + 2), cy - cs, cy - cs, cy - 14},
            9);

    g.setColor(java.awt.Color.GRAY);
    g.fillOval(cx - cs / 2, cy - cs / 2 - 4, cs, cs);
    g.setColor(java.awt.Color.BLACK);
    g.drawOval(cx - cs / 2, cy - cs / 2 - 4, cs, cs);
  }

  @Override
  public void write(org.jhotdraw.util.StorableOutput dw) {
    super.write(dw);
    dw.writeStorable(Wrapper.wrap(rate));
  }

  private Point middlePoint() {
    // if there is no middle point defined, we just return the halfway
    // point ( the default location for the icon ).
    if (pointCount() != 3) {
      Point end = endPoint();
      Point start = startPoint();
      return new Point((start.x + end.x) / 2, (start.y + end.y) / 2);
    } else {
      return pointAt(1);
    }
  }

  private void initializeMiddlePoint() {

    Point end = endPoint();
    Point start = startPoint();
    Point middle = new Point();

    middle.x = (start.x + end.x) / 2;
    middle.y = (start.y + end.y) / 2;

    if (pointCount() == 2) {
      insertPointAt(middle, 1);
    } else {
      setPointAt(middle, 1);
    }
  }


  @Override
  public void read(org.jhotdraw.util.StorableInput dr)
      throws java.io.IOException {
    super.read(dr);
    rate = ((WrappedRate) dr.readStorable()).rate;
  }

  @Override
  public void visit(org.jhotdraw.framework.FigureVisitor visitor) {
    org.jhotdraw.framework.FigureEnumeration f = getDependendFigures();

    visitor.visitFigure(this);

    while (f.hasNextFigure()) {
      org.jhotdraw.framework.Figure fig = f.nextFigure();
      fig.visit(visitor);
    }
  }

  @Override
  public synchronized void removeDependendFigure(Figure oldDependendFigure) {
  }

  @Override
  public int splitSegment(int x, int y) {
    return -1;
  }

  @Override
  public boolean joinSegments(int x, int y) {
    return false;
  }

  @Override
  public void setPointAt(Point p, int i) {
    if (i >= 0) {
      super.setPointAt(p, i);
    }
  }

  /// For org.nlogo.window.Editable

  public scala.Option<String> helpLink() {
    return scala.Option.apply(null);
  }

  public List<Property> propertySet() {
    return Properties.rate();
  }

  private boolean dirty = false;

  public boolean dirty() {
    return dirty;
  }

  public String classDisplayName() {
    return "Flow";
  }

  public boolean editFinished() {
    return true;
  }

  public void nameWrapper(String name) {
    dirty = dirty || !rate.getName().equals(name);
    rate.setName(name);
  }

  public String nameWrapper() {
    return rate.getName();
  }

  public void expressionWrapper(String expression) {
    dirty = dirty || !rate.getExpression().equals(expression);
    rate.setExpression(expression);
  }

  public String expressionWrapper() {
    return rate.getExpression();
  }

  public void bivalentWrapper(boolean bivalent) {
    dirty = dirty || rate.isBivalent() != bivalent;
    rate.setBivalent(bivalent);
  }

  public boolean bivalentWrapper() {
    return rate.isBivalent();
  }

}
