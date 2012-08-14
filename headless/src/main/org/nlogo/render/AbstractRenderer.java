// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

/*

A note on integer math vs. floating point math.

At one time, NetLogo was written in Java 1.1, which didn't have Java2D.
In Java 1.1, everything you did with graphics was with integers.

In Java2D, on the other hand, you can pass doubles instead of ints for
the locations and sizes of things, and Java2D will use anti-aliasing
to make those fractional parts actually have visible effects.

Over time, we have been moving towards using doubles for everything.
I think (I hope) that doubles are now used in all the most important
places.  But there is still some pointless rounding from doubles to
ints that takes place in various places below, for example for things
like the location of the spotlight and the location of labels.
There's no good reason not to fix this, it just hasn't been done yet.
Rounding to ints should never mean that we're off by more than half a
pixel, though, and on things like the halo and labels it seems
unlikely that anyone would ever notice.

If you're wondering if anyone would ever notice if *anything* was half
a pixel off, the answer is definitely yes.  Being half a pixel off can
create easily noticeable artifacts when you are drawing very small
objects, and/or when you are drawing grids of objects, where the grid
structure can make misalignment or inconsistent alignment really jump
out at you visually.  And of course, grids of small objects are very
common in NetLogo models.

So, two conclusions:

  - don't be surprised if you see some occasional carelessness around
    the issue in this code

  - but, more importantly, *DON'T* be careless about it yourself.  In
    the most important parts of the code, for example the sizing and
    positioning of turtles, the code is now very careful to do it
    right.  (I hope I got it right!)  So don't break it.  And whenever
    you are modifying or adding anything, please be very conscientious
    about getting the calculations exactly right.  Let's only move
    towards perfection... never away.

ST 8/19/05

*/

import org.nlogo.api.GraphicsInterface;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.ShapeList;
import org.nlogo.api.TrailDrawerInterface;
import org.nlogo.api.ViewSettings;

public abstract strictfp class AbstractRenderer
    implements org.nlogo.api.RendererInterface {

  public final org.nlogo.api.World world;
  public final LinkDrawer linkDrawer;
  public final TurtleDrawer turtleDrawer;
  private final TrailDrawer _trailDrawer;

  public TrailDrawerInterface trailDrawer() {
    return _trailDrawer;
  }

  public TopologyRenderer topology;
  public final SpotlightDrawer spotlightDrawer = new SpotlightDrawer();

  private boolean _renderLabelsAsRectangles = false;
  public boolean renderLabelsAsRectangles() { return _renderLabelsAsRectangles; }
  public void renderLabelsAsRectangles_$eq(boolean b) { _renderLabelsAsRectangles = b; }

  public AbstractRenderer(org.nlogo.api.World world, ShapeList turtleShapeList, ShapeList linkShapeList) {
    this.world = world;
    linkDrawer = new LinkDrawer(linkShapeList);
    turtleDrawer = new TurtleDrawer(turtleShapeList);
    _trailDrawer = new TrailDrawer(world, turtleDrawer, linkDrawer);
    changeTopology(world.wrappingAllowedInX(), world.wrappingAllowedInY());
  }

  ///

  public void changeTopology(boolean wrapX, boolean wrapY) {
    if (wrapX) {
      if (wrapY) {
        topology = new TorusRenderer(world);
      } else {
        topology = new VertCylinderRenderer(world);
      }
    } else {
      if (wrapY) {
        topology = new HorizCylinderRenderer(world);
      } else {
        topology = new BoxRenderer(world);
      }
    }
    _trailDrawer.setTopology(topology);
  }

  public int getWidth(double patchSize) {
    return (int) StrictMath.round(world.worldWidth() * patchSize);
  }

  public int getHeight(double patchSize) {
    return (int) StrictMath.round(world.worldHeight() * patchSize);
  }

  private static final boolean WINDOWS =
      System.getProperty("os.name").startsWith("Windows");

  org.nlogo.api.Agent outlineAgent;

  public void outlineAgent(org.nlogo.api.Agent agent) {
    outlineAgent = agent;
  }

  ///

  protected abstract void paintPatchLabels(GraphicsInterface g, double patchSize);

  protected abstract void paintTurtles(GraphicsInterface g, double patchSize);

  protected abstract void paintLinks(GraphicsInterface g, double patchSize);

  protected abstract java.awt.image.BufferedImage getSpotlightImage(ViewSettings settings);

  protected abstract boolean anyTurtles();

  public void prepareToPaint(ViewSettings settings, int width, int height) {
    topology.prepareToPaint(settings, width, height);
  }

  ///

  public void paint(GraphicsInterface g, ViewSettings settings) {
    topology.prepareToPaint
        (settings, getWidth(settings.patchSize()), getHeight(settings.patchSize()));
    // now paint turtles & labels
    topology.fillBackground(g);
    paintPatches(g, settings.patchSize());
    // Since the drawing scales when we zoom, even drawing a blank
    // gets expensive very fast. -- 10/06/05 CLB
    // but for some reason on Windows some models run a lot
    // faster on some machines if we uselessly draw the blank
    // layer -- go figure! it's only worth doing if there
    // are turtles though - ST 11/23/05
    if (!_trailDrawer.drawingBlank || (WINDOWS && anyTurtles() && !settings.isHeadless())) {
      topology.paintViewImage
          (g, _trailDrawer.getAndCreateDrawing(false));
    }
    // Turn on accurate stroking for precise subpixel
    // positioning.  On Mac this seems to be the default,
    // but on Windows we need to ask for it. - ST 8/19/05
    g.setStrokeControl();
    paintLinks(g, settings.patchSize());
    paintTurtles(g, settings.patchSize());
    if (settings.drawSpotlight() && spotlightAgent(settings.perspective())) {
      g.drawImage(getSpotlightImage(settings));
    }
  }

  private void paintPatches(GraphicsInterface g, double patchSize) {
    g.antiAliasing(false);
    // first draw the patch colors
    if (world.patchesAllBlack()) {
      topology.paintAllPatchesBlack(g);
    } else {
      setUpPatchImage();
      topology.paintViewImage(g, patchImage);
    }
    // turn on anti-aliasing
    g.antiAliasing(true);
    paintPatchLabels(g, patchSize);
  }

  private int[] patchColors;
  private java.awt.Image patchImage;

  // we're gambling that this will be fast on all systems - ST 11/2/03
  private static final java.awt.image.ColorModel COLOR_MODEL =
      new java.awt.image.DirectColorModel
          (32, 0xff << 16, 0xff << 8, 0xff);

  private void setUpPatchImage() {
    if (patchColors != world.patchColors()) {
      patchColors = world.patchColors();
      patchImage = new java.awt.image.BufferedImage
          (COLOR_MODEL,
              java.awt.image.Raster.createWritableRaster
                  (COLOR_MODEL.createCompatibleSampleModel(world.worldWidth(),
                      world.worldHeight()),
                      new java.awt.image.DataBufferInt(patchColors,
                          patchColors.length),
                      new java.awt.Point(0, 0)),
              true,
              new java.util.Hashtable<String, Object>());
    }
  }

  /// manage the cache

  public void resetCache(double patchSize) {
    turtleDrawer.shapes.resetCache(patchSize);
  }

  public void replaceTurtleShapes(java.util.List<org.nlogo.api.Shape> shapes) {
    turtleDrawer.shapes.shapeList.replaceShapes(shapes);
  }

  public void replaceLinkShapes(java.util.List<org.nlogo.api.Shape> shapes) {
    linkDrawer.linkShapes.replaceShapes(shapes);
  }

  protected boolean darkenPeripheral(ViewSettings settings) {
    return (settings.perspective() == PerspectiveJ.WATCH()) && settings.renderPerspective();
  }

  protected boolean spotlightAgent(Perspective perspective) {
    return (perspective == PerspectiveJ.WATCH() ||
        perspective == PerspectiveJ.FOLLOW() ||
        perspective == PerspectiveJ.RIDE());
  }
}
