package org.nlogo.api;

// implements the GraphicsInterface, wrapper around java.awt.Graphics2D

public strictfp class Graphics2DWrapper
    implements GraphicsInterface {
  private static final boolean IS_MAC =
      System.getProperty("os.name").startsWith("Mac");
  private static final boolean IS_QUARTZ =
      IS_MAC && Boolean.getBoolean("apple.awt.graphics.UseQuartz");

  private final java.awt.Graphics2D g;

  public Graphics2DWrapper(java.awt.Graphics2D g) {
    this.g = g;
  }

  public String location(double x, double y) {
    return "(" + (g.getTransform().getTranslateX() + x) + " , " + (g.getTransform().getTranslateY() + y) + ")";
  }

  public void draw(java.awt.Shape shape) {
    g.draw(shape);
  }

  public void drawImage(java.awt.image.BufferedImage image) {
    g.drawImage(image, null, 0, 0);
  }

  public void drawImage(java.awt.Image image, int x, int y, int width, int height) {
    g.drawImage(image, x, y, width, height, null);
  }

  public void drawLine(double x1, double y1, double x2, double y2) {
    g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
  }

  public void drawLabel(String label, double x, double y, double patchSize) {
    java.awt.FontMetrics fm = g.getFontMetrics();
    g.translate(x - fm.stringWidth(label), 0);
    if (patchSize >= (fm.getMaxAscent() + fm.getMaxDescent())) {
      g.translate(0, y - fm.getMaxDescent());
    } else // maxAscent is centered on the patch
    {
      double centerAdjustment = StrictMath.min(0, (patchSize / 4) - (fm.getMaxAscent() / 4));
      g.translate(0, y - centerAdjustment);
    }
    g.drawString(label, 0, 0);
  }

  public void fillCircle(double x, double y, double xDiameter, double yDiameter, double scale, double angle) {
    double sizeCorrection = 0;
    double xCorrection = 0;
    double yCorrection = 0;
    if (IS_QUARTZ) {
      // one pixel bigger
      sizeCorrection = Constants.ShapeWidth() / scale;
      // adjust position to still be centered
      xCorrection = -0.5 * sizeCorrection;
      yCorrection = xCorrection;
    }

    g.fill(new java.awt.geom.Ellipse2D.Double
        (x + xCorrection, y + yCorrection,
            xDiameter + sizeCorrection, yDiameter + sizeCorrection));
  }

  public void drawCircle(double x, double y, double xDiameter, double yDiameter, double scale, double angle) {
    double sizeCorrection = 0;
    double xCorrection = 0;
    double yCorrection = 0;

    if (!IS_QUARTZ) {
      // one pixel smaller
      sizeCorrection = -Constants.ShapeWidth() / scale;
      xCorrection = getXCorrection(sizeCorrection, angle);
      yCorrection = getYCorrection(sizeCorrection, angle);
    }

    g.draw(new java.awt.geom.Ellipse2D.Double
        (x + xCorrection, y + yCorrection,
            xDiameter + sizeCorrection, yDiameter + sizeCorrection));
  }

  public void fillRect(double x, double y, double width, double height,
                       double scale, double angle) {
    double sizeCorrection = 0;
    double xCorrection = 0;
    double yCorrection = 0;

    if (IS_QUARTZ) {
      // size: one pixel bigger
      sizeCorrection = Constants.ShapeWidth() / scale;
      xCorrection = getXCorrection(sizeCorrection, angle);
      yCorrection = getYCorrection(sizeCorrection, angle);
    }

    g.fill(new java.awt.geom.Rectangle2D.Double
        (x + xCorrection, y + yCorrection,
            width + sizeCorrection, height + sizeCorrection));
  }

  public void drawRect(double x, double y, double width, double height,
                       double scale, double angle) {
    double sizeCorrection = 0;
    if (!IS_QUARTZ) {
      // size: one pixel smaller
      sizeCorrection = -Constants.ShapeWidth() / scale;
    }

    g.draw(new java.awt.geom.Rectangle2D.Double
        (x, y, width + sizeCorrection, height + sizeCorrection));
  }

  // as for the corrections to the position, well, I
  // wrote it, but that doesn't mean I understand
  // it.  You wouldn't believe the amount of
  // guesswork and trial and error that went into
  // this. Basically I figured out what the answers
  // should be for the cases where angle is a multiple
  // of 90, and then I tried to come up with formulas
  // based on sin and cos that would give the answers
  // I expected for those four cases, and then hoped
  // they'd work for intermediate angles too.  Which
  // they seem to. - ST 8/16/05, 8/25/05
  private double getXCorrection(double sizeCorrection, double angle) {
    return angle == 0 ? 0 :
        (sizeCorrection * (StrictMath.cos((angle + 135) / 180.0 * StrictMath.PI) + 0.7071067811865476)
            / -1.4142135623730951);
  }

  private double getYCorrection(double sizeCorrection, double angle) {
    return angle == 0 ? 0 :
        (sizeCorrection * (StrictMath.sin((angle - 45) / 180.0 * StrictMath.PI) + 0.7071067811865476)
            / -1.4142135623730951);
  }

  public void fill(java.awt.Shape shape) {
    g.fill(shape);
  }

  public void fillRect(int x, int y, int width, int height) {
    g.fillRect(x, y, width, height);
  }

  private final java.util.LinkedList<java.awt.geom.AffineTransform> transforms
      = new java.util.LinkedList<java.awt.geom.AffineTransform>();

  private final java.util.LinkedList<java.awt.Stroke> strokes =
      new java.util.LinkedList<java.awt.Stroke>();

  public void pop() {
    g.setTransform(transforms.removeLast());
    g.setStroke(strokes.removeLast());
  }

  public void push() {
    transforms.addLast(g.getTransform());
    strokes.addLast(g.getStroke());
  }

  public void rotate(double theta) {
    g.rotate(theta);
  }

  public void rotate(double theta, double x, double y) {
    g.rotate(theta, x, y);
  }

  public void rotate(double theta, double x, double y, double offset) {
    if (IS_QUARTZ) {
      offset -= 1;
    }
    g.rotate(theta, x + offset / 2, y + offset / 2);
  }

  public void scale(double x, double y) {
    g.scale(x, y);
  }

  public void scale(double x, double y, double shapeWidth) {
    if (IS_QUARTZ) {
      x -= 1;
      y -= 1;
    }
    g.scale(x / shapeWidth, y / shapeWidth);
  }

  public void antiAliasing(boolean on) {
    g.setRenderingHint
        (java.awt.RenderingHints.KEY_ANTIALIASING,
            (on ? java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                : java.awt.RenderingHints.VALUE_ANTIALIAS_OFF));
  }

  public void setInterpolation() {
    // on Macs we need this or we get blurry scaling,
    // but on Windows we can't do it or it kills performance
    // - ST 11/2/03
    if (IS_MAC) {
      g.setRenderingHint
          (java.awt.RenderingHints.KEY_INTERPOLATION,
              java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }
  }

  public void setStrokeControl() {
    g.setRenderingHint
        (java.awt.RenderingHints.KEY_STROKE_CONTROL,
            java.awt.RenderingHints.VALUE_STROKE_PURE);
  }

  public void setColor(java.awt.Color c) {
    g.setColor(c);
  }

  public void setComposite(java.awt.Composite comp) {
    g.setComposite(comp);
  }

  public void setStroke(double width) {
    g.setStroke(new java.awt.BasicStroke((float) StrictMath.max(1, width)));
  }

  public void setStrokeFromLineThickness(double lineThickness, double scale, double cellSize, double shapeWidth) {
    if (IS_QUARTZ) {
      scale -= 1;
    }
    setStroke((shapeWidth / scale) *
        (lineThickness == 0
            ? 1
            : (lineThickness * cellSize)));
  }

  public void setStroke(float width, float[] dashes) {
    g.setStroke(new java.awt.BasicStroke(width, java.awt.BasicStroke.CAP_ROUND,
        java.awt.BasicStroke.JOIN_ROUND, 1.0f, dashes, 0));
  }

  public void setPenWidth(double penSize) {
    float width = (float) StrictMath.max(1, penSize);
    if (((java.awt.BasicStroke) g.getStroke()).getLineWidth() != width) {
      g.setStroke
          (new java.awt.BasicStroke(width,
              java.awt.BasicStroke.CAP_ROUND,
              java.awt.BasicStroke.JOIN_MITER));
    }
  }

  public void translate(double x, double y) {
    g.translate(x, y);
  }

  public void drawPolygon(int[] xcors, int[] ycors, int length) {
    g.drawPolygon(xcors, ycors, length);
  }

  public void fillPolygon(int[] xcors, int ycors[], int length) {
    g.fillPolygon(xcors, ycors, length);
  }

  public void drawPolyline(int[] xcors, int[] ycors, int length) {
    g.drawPolyline(xcors, ycors, length);
  }

  public void dispose() {
    g.dispose();
  }

  @Override
  public java.awt.FontMetrics getFontMetrics() {
    return g.getFontMetrics();
  }
}
