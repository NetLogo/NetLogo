// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape;

import org.nlogo.api.GraphicsInterface;

import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.util.StringTokenizer;

public strictfp class LinkLine
    implements java.io.Serializable, Cloneable {
  static final long serialVersionUID = 0L;

  private float[] dashes;
  private double xcor = 0;
  private boolean isVisible = false;

  public static final float[][] dashChoices
      = {{0.0f, 1.0f}, {1.0f, 0.0f}, {2.0f, 2.0f},
      {4.0f, 4.0f}, {4.0f, 4.0f, 2.0f, 2.0f}};

  public int dashIndex() {
    for (int i = 0; i < dashChoices.length; i++) {
      if (dashes == dashChoices[i]) {
        return i;
      }
    }

    return 1;
  }

  public LinkLine() {
    dashes = dashChoices[1];
  }

  public LinkLine(double xcor, boolean visible) {
    this.xcor = xcor;
    isVisible = visible;
    dashes = dashChoices[(visible ? 1 : 0)];
  }

  public boolean isStraightPlainLine() {
    return isVisible && (xcor == 0) && (dashes.length == 2)
        && (dashes[0] == 1) && (dashes[1] == 0);
  }

  public boolean isVisible() {
    return isVisible;
  }

  public void setVisible(boolean isVisible) {
    this.isVisible = isVisible;
  }

  public void paint(GraphicsInterface g, java.awt.Color color,
                    double cellSize, float strokeWidth,
                    java.awt.Shape shape) {
    g.setColor(color);
    g.setStroke(strokeWidth, dashes);
    g.draw(shape);
  }

  public java.awt.Shape getShape(double x1, double y1, double x2, double y2,
                                 double curviness, double size, double cellSize,
                                 float stroke) {
    double ycomp = (x1 - x2) / size;
    double xcomp = (y2 - y1) / size;
    AffineTransform trans = AffineTransform.getTranslateInstance
        (xcomp * xcor * stroke, ycomp * xcor * stroke);
    double midX = ((x1 + x2) / 2) + (curviness * xcomp);
    double midY = ((y1 + y2) / 2) + (curviness * ycomp);
    return trans.createTransformedShape
        (new QuadCurve2D.Double(x1, y1, midX, midY, x2, y2));
  }

  public float[] getDashes() {
    return dashes;
  }

  public void setDashiness(float[] dashes) {
    this.dashes = dashes;
  }

  public void setDashes(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    dashes = new float[tokenizer.countTokens()];
    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
      dashes[i] = Float.parseFloat(tokenizer.nextToken());
    }
  }

  public String dashinessString() {
    String result = "";
    for (int i = 0; i < dashes.length; i++) {
      if (result.length() > 0) {
        result += " ";
      }
      result += dashes[i];
    }
    return result;
  }

  public double xcor() {
    return xcor;
  }

  @Override
  public Object clone() {
    LinkLine line;
    try {
      line = (LinkLine) super.clone();
    } catch (CloneNotSupportedException ex) {
      // should never happen since we implement Cloneable
      throw new IllegalStateException(ex);
    }
    line.dashes = new float[dashes.length];
    System.arraycopy(dashes, 0, line.dashes, 0, dashes.length);
    return line;
  }

  @Override
  public String toString() {
    return xcor + " " + (isVisible ? "1" : "0") + " " + dashinessString();
  }

  public String toReadableString() {
    return "Link Line with xcor = " + xcor + " " + isVisible + " " + dashinessString();
  }

  private static int getDashIndex(float[] d2) {
    boolean success = false;
    for (int i = 0; i < dashChoices.length; i++) {
      if (d2.length == dashChoices[i].length) {
        for (int j = 0; j < d2.length; j++) {
          success = d2[j] == dashChoices[i][j];
        }
        if (success) {
          return i;
        }
      }
    }
    return 1;
  }

  public static int parseLine(String[] shapes, String version, LinkLine line, int index) {
    StringTokenizer tokenizer = new StringTokenizer(shapes[index]);
    line.xcor = Double.parseDouble(tokenizer.nextToken());
    line.isVisible = Integer.parseInt(tokenizer.nextToken()) != 0;
    float[] d = new float[tokenizer.countTokens()];
    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
      d[i] = Float.parseFloat(tokenizer.nextToken());
    }
    line.dashes = dashChoices[getDashIndex(d)];
    return ++index;
  }
}
