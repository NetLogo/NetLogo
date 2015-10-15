// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.Graphics2DWrapper;
import org.nlogo.api.GraphicsInterface;

public strictfp class SpotlightDrawer
    implements Drawable {
  private java.awt.image.BufferedImage spotlightImage;


  public java.awt.image.BufferedImage getImage(TopologyRenderer topology, double xcor, double ycor,
                                               int width, int height, double patchSize,
                                               double spotlightSize, boolean dim, boolean wrap) {
    // set up the spotlight image
    if (spotlightImage == null ||
        spotlightImage.getWidth() != width ||
        spotlightImage.getHeight() != height) {
      spotlightImage = new java.awt.image.BufferedImage
          (width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }

    Graphics2DWrapper sg = new Graphics2DWrapper(spotlightImage.createGraphics());
    sg.antiAliasing(true);

    if (dim) {
      sg.setComposite(java.awt.AlphaComposite.Src);
      sg.setColor(DIMMED);
      sg.fillRect(0, 0, width, height);
    } else {
      sg.setComposite(java.awt.AlphaComposite.Clear);
      sg.setColor(DIMMED);
      sg.fillRect(0, 0, width, height);
    }

    if (wrap) {
      topology.wrapDrawable(this, sg, xcor, ycor, spotlightSize, patchSize);
    } else {
      sg.push();
      double size = spotlightSize * patchSize + adjustSize(spotlightSize, patchSize);
      double offset = size / 2.0;
      double x = topology.graphicsX(xcor, patchSize) - offset;
      double y = topology.graphicsY(ycor, patchSize) - offset;
      sg.translate(x, y);
      draw(sg, size);
      sg.pop();
    }

    sg.antiAliasing(false);

    return spotlightImage;
  }

  // Don't let the spotlight be smaller than 10% of total view height / width
  public double adjustSize(double spotlightSize, double patchSize) {
    double minSize = StrictMath.max(spotlightImage.getWidth(), spotlightImage.getHeight()) / 20;
    return (spotlightSize * patchSize) < minSize ? (minSize - spotlightSize) : 0;
  }

  static java.awt.Color DIMMED =
      new java.awt.Color(0, 0, 50, 100);
  private static final java.awt.Color SPOTLIGHT_INNER_BORDER =
      new java.awt.Color(200, 255, 255, 100);
  private static final java.awt.Color SPOTLIGHT_OUTER_BORDER =
      new java.awt.Color(200, 255, 255, 50);

  private static final double OUTER = 10;
  private static final double MIDDLE = 8;
  private static final double INNER = 4;

  private static final java.awt.geom.Ellipse2D.Double ellipse = new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1);

  public void draw(GraphicsInterface g, double size) {
    // Clear out area for spotlight
    g.setComposite(java.awt.AlphaComposite.Clear);
    drawEllipse(g, MIDDLE + size, -MIDDLE / 2);

    // halo is composed of three overlapping circles
    g.setComposite(java.awt.AlphaComposite.Src);

    g.setColor(DIMMED);
    drawEllipse(g, OUTER + size, -OUTER / 2);

    g.setColor(SPOTLIGHT_OUTER_BORDER);
    drawEllipse(g, MIDDLE + size, -MIDDLE / 2);

    g.setColor(SPOTLIGHT_INNER_BORDER);
    drawEllipse(g, INNER + size, -INNER / 2);

    // this middle of the spotlight is clear
    g.setComposite(java.awt.AlphaComposite.Clear);
    drawEllipse(g, size, 0);
  }

  private void drawEllipse(GraphicsInterface g, double size, double offset) {
    g.push();
    g.translate(offset, offset);
    g.scale(size, size);
    g.fill(ellipse);
    g.pop();
  }
}
