// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.GraphicsInterface;
import org.nlogo.api.Turtle;
import org.nlogo.shape.VectorShape;

// public because the HubNet client uses it - ST 3/1/05
public strictfp class TurtleDrawer {
  private static final double MIN_PATCH_SIZE_FOR_TURTLE_SHAPES = 3.0;

  final TurtleShapeManager shapes;

  public TurtleDrawer(org.nlogo.api.ShapeList shapeList) {
    shapes = new TurtleShapeManager(shapeList);
  }

  public void drawTurtle(GraphicsInterface g, TopologyRenderer topology,
                         org.nlogo.api.Turtle turtle, double patchSize) {
    if (!turtle.hidden()) {
      if (turtle.size() * patchSize >= MIN_PATCH_SIZE_FOR_TURTLE_SHAPES) {
        drawTurtleShape(g, topology, turtle, patchSize);
      } else {
        topology.drawWrappedRect(g, org.nlogo.api.Color.getColor(turtle.color()),
            0.0f, turtle.xcor(), turtle.ycor(), turtle.size(), patchSize, true);
      }
      if (turtle.hasLabel()) {
        drawTurtleLabel(g, topology, turtle, patchSize);
      }
    }
  }

  void drawTurtleShape(GraphicsInterface g, TopologyRenderer topology, org.nlogo.api.Turtle turtle, double patchSize) {
    Drawable d = getShapeFromCacheOrCreateDrawable(turtle, patchSize, shapes.getShape(turtle));
    topology.wrapDrawable(d, g, turtle.xcor(), turtle.ycor(), turtle.size(), patchSize);
  }

  private Drawable getShapeFromCacheOrCreateDrawable(Turtle turtle, double patchSize, VectorShape shape) {
    if (shapes.useCache(turtle, patchSize) && !shape.isTooSimpleToCache()) {
      java.awt.Color turtleColor = org.nlogo.api.Color.getColor(turtle.color());
      // if the shape isn't recolorable, then there's no need to consider
      // the turtle's color as part of the cache key, so just always
      // use white as a dummy key in that case - ST 9/3/03

      // but we do need to consider the turtles transparency.
      // the good news is that transparency isnt used as part of the key
      // on the call to shapes.getCachedShape
      // the correct shape will be found in the cache, and will inherit
      // the correct alpha level from the turtle. - JC 3/8/2010
      java.awt.Color fgColor =
          shape.fgRecolorable()
              ? turtleColor
              : new java.awt.Color(255, 255, 255, turtleColor.getAlpha());
      return shapes.getCachedShape(shape, fgColor, turtle.heading(), turtle.size());
    } else {
      return new VectorShapeDrawable
          (shape, org.nlogo.api.Color.getColor(turtle.color()),
              patchSize, (int) turtle.heading(), turtle.lineThickness(), turtle.size());
    }
  }

  void drawTurtleWithOutline(GraphicsInterface g, TopologyRenderer topology,
                             org.nlogo.api.Turtle turtle, double patchSize) {
    if (!turtle.hidden()) {
      if (turtle.size() * patchSize >= MIN_PATCH_SIZE_FOR_TURTLE_SHAPES) {
        drawTurtleShapeWithOutline(g, topology, turtle, patchSize);
      } else {
        drawWrappedRectWithOutline(g, topology, turtle, patchSize);
      }
      if (turtle.hasLabel()) {
        drawTurtleLabel(g, topology, turtle, patchSize);
      }
    }
  }

  private void drawTurtleShapeWithOutline(GraphicsInterface g, TopologyRenderer topology,
                                          org.nlogo.api.Turtle turtle, double patchSize) {
    double turtleSize = turtle.size();
    double xcor = turtle.xcor();
    double ycor = turtle.ycor();

    VectorShape shape = shapes.getShape(turtle);
    VectorShape outline = (VectorShape) shape.clone();
    outline.setOutline();

    double thickness = StrictMath.min(turtleSize / 5, 0.5);

    java.awt.Color color = org.nlogo.api.Color.getColor(turtle.color());
    int heading = (int) turtle.heading();
    topology.wrapDrawable
        (new VectorShapeDrawable
            (outline, color, patchSize, heading, thickness, turtleSize),
            g, turtle.xcor(), turtle.ycor(), turtleSize, patchSize);

    topology.wrapDrawable
        (new VectorShapeDrawable
            (outline, org.nlogo.api.Color.getComplement(color), patchSize, heading, thickness / 2, turtleSize),
            g, xcor, ycor, turtleSize, patchSize);

    topology.wrapDrawable
        (new VectorShapeDrawable
            (shape, color, patchSize, heading, turtle.lineThickness(), turtleSize),
            g, xcor, ycor, turtleSize, patchSize);
  }

  private void drawWrappedRectWithOutline(GraphicsInterface g, TopologyRenderer topology,
                                          org.nlogo.api.Turtle turtle, double patchSize) {
    double xcor = turtle.xcor();
    double ycor = turtle.ycor();
    double turtleSize = turtle.size();

    java.awt.Color color = org.nlogo.api.Color.getColor(turtle.color());
    topology.drawWrappedRect(g, color, 4.0f, xcor, ycor, turtleSize, patchSize, false);
    topology.drawWrappedRect(g, org.nlogo.api.Color.getComplement(color), 2.0f,
        xcor, ycor, turtleSize, patchSize, false);
    topology.drawWrappedRect(g, color, (float) turtleSize, xcor, ycor, turtleSize, patchSize, true);
  }

  private void drawTurtleLabel(GraphicsInterface g, TopologyRenderer topology, org.nlogo.api.Turtle turtle, double patchSize) {
    topology.drawLabelHelper
        (g, turtle.xcor(), turtle.ycor(), turtle.labelString(), turtle.labelColor(), patchSize, turtle.size());
  }
}
