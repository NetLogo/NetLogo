// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;
import org.nlogo.shape.LinkShape;

public strictfp class LinkDrawer {
  final org.nlogo.api.ShapeList linkShapes;

  public LinkDrawer(org.nlogo.api.ShapeList linkShapes) {
    this.linkShapes = linkShapes;
  }

  public void drawLink(GraphicsInterface g, TopologyRenderer topology, org.nlogo.api.Link link,
                       double patchSize, boolean outline) {
    if (!link.hidden()) {
      if (link.size() > 0) {
        if (outline) {
          drawLinkWithOutline(g, topology, link, patchSize);
        } else {
          drawLink(g, topology, link, patchSize);
        }
      }

      if (link.hasLabel()) {
        drawLinkLabel(g, topology, link, patchSize);
      }
    }
  }

  void drawLink(GraphicsInterface g, TopologyRenderer topology, org.nlogo.api.Link link,
                double patchSize) {
    topology.drawLink(g, link, getLinkDrawable(link), patchSize,
        org.nlogo.api.Color.getColor(link.color()), link.lineThickness());
  }

  private void drawLinkWithOutline(GraphicsInterface g, TopologyRenderer topology, org.nlogo.api.Link link,
                                   double patchSize) {
    LinkDrawable drawer = getLinkDrawable(link);
    double lineThickness = link.lineThickness();
    java.awt.Color color = org.nlogo.api.Color.getColor(link.color());
    topology.drawLink(g, link, drawer, patchSize, color, lineThickness + (4 / patchSize));
    topology.drawLink(g, link, drawer, patchSize, org.nlogo.api.Color.getComplement(color),
        lineThickness + (2 / patchSize));
    topology.drawLink(g, link, drawer, patchSize, color, lineThickness);
  }

  private LinkDrawable getLinkDrawable(org.nlogo.api.Link link) {
    LinkShape shape = (LinkShape) linkShapes.shape(link.shape());

    if (shape.isTooSimpleToPaint() && !link.isDirectedLink()) {
      return new LineDrawer();
    } else if (shape.isTooSimpleToPaint()) {
      return new SimpleShapeDrawer(shape);
    } else {
      return new LinkShapeDrawer(shape);
    }
  }

  private void drawLinkLabel(GraphicsInterface g, TopologyRenderer topology, org.nlogo.api.Link link, double patchSize) {
    double midx = labelX(link, topology);
    double midy = labelY(link, topology);

    topology.drawLabelHelper(g, midx, midy, link.labelString(), link.labelColor(), patchSize, 1);
  }

  private double labelX(org.nlogo.api.Link link, TopologyRenderer topology) {
    if (!link.isDirectedLink()) {
      return link.midpointX() - 0.5;
    } else {
      return pointBetweenMidpointAndEnd2X(link, topology, 0.5) + 0.5;
    }
  }

  private double labelY(org.nlogo.api.Link link, TopologyRenderer topology) {
    if (!link.isDirectedLink()) {
      return link.midpointY() + 0.5;
    } else {
      return pointBetweenMidpointAndEnd2Y(link, topology, 0.5) + 0.5;
    }
  }

  private double pointBetweenMidpointAndEnd2X(org.nlogo.api.Link link, TopologyRenderer topology, double c) {
    double x1 = (link.x1() + link.x2()) / 2;
    double x2 = link.x2() - StrictMath.sin(StrictMath.toRadians(link.heading())) * (link.linkDestinationSize() - 1);
    double xdiff = x1 - x2;
    return topology.wrapX(x1 - (xdiff * c));
  }

  private double pointBetweenMidpointAndEnd2Y(org.nlogo.api.Link link, TopologyRenderer topology, double c) {
    double y1 = (link.y1() + link.y2()) / 2;
    double y2 = link.y2() - StrictMath.cos(StrictMath.toRadians(link.heading())) * (link.linkDestinationSize() - 1);
    double ydiff = y1 - y2;
    return topology.wrapY(y1 - (ydiff * c));
  }

  public interface LinkDrawable {
    void draw(GraphicsInterface g, org.nlogo.api.Link link, java.awt.Color color,
              double x1, double y1, double x2, double y2,
              double patchSize, double lineThickness);
  }

  private class LineDrawer
      implements LinkDrawable {
    public void draw(GraphicsInterface g, org.nlogo.api.Link link, java.awt.Color color,
                     double x1, double y1, double x2, double y2,
                     double patchSize, double lineThickness) {
      g.drawLine(x1, y1, x2, y2);
    }
  }

  private class SimpleShapeDrawer
      implements LinkDrawable {
    private final LinkShape shape;

    SimpleShapeDrawer(LinkShape shape) {
      this.shape = shape;
    }

    public void draw(GraphicsInterface g, org.nlogo.api.Link link, java.awt.Color color,
                     double x1, double y1, double x2, double y2,
                     double patchSize, double lineThickness) {
      g.drawLine(x1, y1, x2, y2);

      shape.paintDirectionIndicator
          (g, color, x1, y1, x2, y2, link.heading(), patchSize,
              lineThickness, link.linkDestinationSize(), link.size());
    }
  }

  private class LinkShapeDrawer
      implements LinkDrawable {
    private final LinkShape shape;

    LinkShapeDrawer(LinkShape shape) {
      this.shape = shape;
    }

    public void draw(GraphicsInterface g, org.nlogo.api.Link link, java.awt.Color color,
                     double x1, double y1, double x2, double y2,
                     double patchSize, double lineThickness) {
      shape.paint(g, color, x1, y1, x2, y2,
          patchSize, link.size(), lineThickness,
          link.linkDestinationSize(), link.isDirectedLink());
    }
  }
}
