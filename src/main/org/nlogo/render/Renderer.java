// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.Agent;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Graphics2DWrapper;
import org.nlogo.api.GraphicsInterface;
import org.nlogo.api.Link;
import org.nlogo.api.Patch;
import org.nlogo.api.Turtle;
import org.nlogo.api.ViewSettings;
import org.nlogo.api.World;

public strictfp class Renderer
    extends AbstractRenderer {

  public Renderer(World world) {
    this(world, false);
  }

  public Renderer(World world, boolean renderLabelsAsRectangles) {
    super(world, world.turtleShapeList(), world.linkShapeList());
    renderLabelsAsRectangles_$eq(renderLabelsAsRectangles);
  }

  ///
  public void paint(java.awt.Graphics2D g, ViewSettings settings) {
    paint(new Graphics2DWrapper(g, renderLabelsAsRectangles()), settings);
  }

  @Override
  protected void paintPatchLabels(GraphicsInterface g, double patchSize) {
    // then draw the patch labels
    if (world.patchesWithLabels() > 0) {
      int patchCount = world.patches().count();
      for (int i = 0; i < patchCount; i++) {
        Patch patch = world.getPatch(i);
        if (patch.hasLabel()) {
          drawPatchLabel(g, patch, patchSize);
        }
      }
    }
    if (outlineAgent instanceof Patch) {
      Patch patch = (Patch) outlineAgent;
      topology.drawWrappedRect(g,
          org.nlogo.api.Color.getComplement
              (org.nlogo.api.Color.getColor(patch.pcolor())),
          1.0f, patch.pxcor(), patch.pycor(), 1, patchSize, false);
    }
  }

  @Override
  protected void paintLinks(GraphicsInterface g, double patchSize) {
    int linksDrawn = 0;
    for (scala.collection.Iterator<String> iter = world.program().linkBreeds().keys().iterator();
         iter.hasNext();) {
      AgentSet breed = world.getLinkBreed(iter.next());
      for (Agent a : breed.agents()) {
        linkDrawer.drawLink(g, topology, (Link) a, patchSize, false);
        linksDrawn++;
      }
    }
    if (linksDrawn < world.links().count()) {
      // uh oh, we have some unbreeded turtles we need to go back and draw
      for (Agent a : world.links().agents()) {
        Link link = (Link) a;
        if (link.getBreed() == world.links()) {
          linkDrawer.drawLink(g, topology, link, patchSize, false);
        }
      }
    }
    if (outlineAgent instanceof Link) {
      linkDrawer.drawLink(g, topology, (Link) outlineAgent, patchSize, true);
    }
  }

  @Override
  protected void paintTurtles(GraphicsInterface g, double patchSize) {
    int turtlesDrawn = 0;
    // traverse breeds in reverse order of declaration
    for (scala.collection.Iterator<String> iter = world.program().breeds().keys().iterator();
         iter.hasNext();) {
      AgentSet breed = world.getBreed(iter.next());
      if (breed.kind() == AgentKindJ.Turtle()) {
        for (Agent a : breed.agents()) {
          turtleDrawer.drawTurtle(g, topology, (Turtle) a, patchSize);
          turtlesDrawn++;
        }
      }
    }
    if (turtlesDrawn < world.turtles().count()) {
      // uh oh, we have some unbreeded turtles we need to go back and draw
      for (Agent a : world.turtles().agents()) {
        Turtle turtle = (Turtle) a;
        if (turtle.getBreed() == world.turtles()) {
          turtleDrawer.drawTurtle(g, topology, turtle, patchSize);
        }
      }
    }
    if (outlineAgent instanceof Turtle) {
      turtleDrawer.drawTurtleWithOutline(g, topology, (Turtle) outlineAgent, patchSize);
    }
  }

  /// Labels

  private void drawPatchLabel(GraphicsInterface g, Patch patch, double patchSize) {
    topology.drawLabelHelper(g, patch.pxcor(), patch.pycor(),
        patch.labelString(), patch.labelColor(),
        patchSize, 1);
  }

  Agent targetAgent() {
    return world.observer().targetAgent();
  }

  @Override
  protected java.awt.image.BufferedImage getSpotlightImage(ViewSettings settings) {
    double xcor, ycor, spotlightSize;
    boolean wrap = false;
    Agent agent = targetAgent();

    if (agent instanceof Turtle) {
      Turtle turtle = (Turtle) agent;
      spotlightSize = turtle.size() * 2;
      xcor = turtle.xcor();
      ycor = turtle.ycor();
      wrap = true;
    } else if (agent instanceof Link) {
      Link link = (Link) agent;
      spotlightSize = link.size();
      xcor = link.midpointX();
      ycor = link.midpointY();
    } else {
      Patch patch = (Patch) agent;
      spotlightSize = 2;
      xcor = patch.pxcor();
      ycor = patch.pycor();
    }

    return spotlightDrawer.getImage
        (topology, xcor, ycor, getWidth(settings.patchSize()), getHeight(settings.patchSize()),
            settings.patchSize(), spotlightSize, darkenPeripheral(settings), wrap);
  }

  @Override
  protected boolean anyTurtles() {
    return world.turtles().count() > 0;
  }

  /// NetLogo coords -> pixel coords

  public double graphicsX(double xcor, double patchSize, double viewOffsetX) {
    return topology.graphicsX(xcor, patchSize, viewOffsetX);
  }

  public double graphicsY(double ycor, double patchSize, double viewOffsetY) {
    return topology.graphicsY(ycor, patchSize, viewOffsetY);
  }

  ///

  public java.awt.image.BufferedImage exportView(ViewSettings settings) {
    // unfortunately we can't just call org.nlogo.awt.Images.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call graphicsPainter's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04
    java.awt.image.BufferedImage image =
        new java.awt.image.BufferedImage
            (getWidth(settings.patchSize()), getHeight(settings.patchSize()),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g = (java.awt.Graphics2D) image.getGraphics();
    java.awt.Font font = g.getFont();
    java.awt.Font newFont =
        new java.awt.Font(font.getName(), font.getStyle(), settings.fontSize());
    g.setFont(newFont);

    synchronized (world) {
      paint(g, settings);
    }
    return image;
  }

  public void exportView(java.awt.Graphics2D g, ViewSettings settings) {
    // unfortunately we can't just call org.nlogo.awt.Images.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call graphicsPainter's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04, 10/12/05
    synchronized (world) {
      paint(g, settings);
    }
  }
}
