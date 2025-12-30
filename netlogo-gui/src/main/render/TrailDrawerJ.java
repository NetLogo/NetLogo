// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render;

import org.nlogo.api.Graphics2DWrapper;
import org.nlogo.api.WorkspaceContext;

public abstract class TrailDrawerJ
    implements org.nlogo.api.TrailDrawerInterface,
    org.nlogo.api.ViewSettings {
  private final org.nlogo.api.World world;
  protected java.awt.image.BufferedImage drawingImage = null;
  protected int width;
  protected int height;
  int[] colorArray = null;
  public TopologyRenderer topology;
  private final TurtleDrawer turtleDrawer;
  private final LinkDrawer linkDrawer;

  public TrailDrawerJ(org.nlogo.api.World world, TurtleDrawer turtleDrawer, LinkDrawer linkDrawer) {
    this.world = world;
    this.turtleDrawer = turtleDrawer;
    this.linkDrawer = linkDrawer;
  }

  // for rendering drawing in 3D view
  protected boolean drawingDirty = false;
  protected boolean drawingBlank = true;

  @Override
  public int[] colors() {
    if (drawingDirty) {
      if (colorArray == null) {
        colorArray = new int[width * height * 4];
      }
      java.awt.image.Raster raster = drawingImage.getRaster();
      // note that the docs say the output array should be allocated
      // by getDataElements if it is null, my experience is it doesn't. ev 9/21/05
      raster.getDataElements(0, 0, width, height, colorArray);
    }

    return colorArray;
  }

  public void setTopology(TopologyRenderer topology) {
    this.topology = topology;
  }

  public boolean sendPixels() {
    return _sendPixels;
  }

  public void sendPixels(boolean dirty) {
    _sendPixels = dirty;
  }

  protected boolean _sendPixels = false;

  public boolean isDirty() {
    return drawingDirty;
  }

  public boolean isBlank() {
    return drawingBlank;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void markDirty() {
    drawingBlank = false;
    drawingDirty = true;
  }

  public void markClean() {
    drawingDirty = false;
  }

  public Object getDrawing() {
    return drawingImage;
  }

  public void readImage(java.awt.image.BufferedImage image)
      throws java.io.IOException {
    java.awt.Graphics2D dg = drawingImage.createGraphics();

    dg.drawImage(image, 0, 0, null);

    drawingBlank = false;
    drawingDirty = true;
  }

  public void stamp(org.nlogo.api.Agent agent, boolean erase) {
    Graphics2DWrapper tg = new Graphics2DWrapper(getAndCreateDrawing(false).createGraphics());
    tg.antiAliasing(true);
    tg.setComposite(erase ? java.awt.AlphaComposite.Clear : java.awt.AlphaComposite.SrcOver);

    topology.prepareToPaint(this, width, height);

    // we use world.fixedPatchSize here because the drawing does not
    // change resolution due to zooming. ev 4/2/08
    if (agent instanceof org.nlogo.api.Turtle) {
      turtleDrawer.drawTurtleShape
          (tg, topology, (org.nlogo.api.Turtle) agent, world.fixedPatchSize());
    } else if (agent instanceof org.nlogo.api.Link) {
      linkDrawer.drawLink
          (tg, topology, (org.nlogo.api.Link) agent, world.fixedPatchSize(), false);
    }
    tg.antiAliasing(false);

    markDirty();
  }

  protected void drawWrappedLine(Graphics2DWrapper tg,
                                 double x1, double y1, double x2, double y2,
                                 double penSize) {
    double startX = x1; // .016
    double startY = y1; // 16
    double endX = x1;   // .016
    double endY = y1;   // 16
    double temp;
    // these are never called
    if (endX < startX) {
      temp = endX;
      endX = startX;
      startX = temp;
    }
    if (endY < startY) {
      temp = endY;
      endY = startY;
      startY = temp;
    }

    double xdiff = x2 - x1;
    double ydiff = y2 - y1;
    double distX = x2 - x1;
    double distY = y2 - y1;
    double newStartX = 0;
    double newStartY = 0;
    double maxy = world.maxPycor() + 0.4999999;
    double maxx = world.maxPxcor() + 0.4999999;
    double miny = world.minPycor() - 0.5;
    double minx = world.minPxcor() - 0.5;
    double pixelSize = 1 / world.fixedPatchSize();

    int count = 0;

    do {
      endX = startX + distX;
      endY = startY + distY;

      if (endY < miny) {
        endY = miny;
        endX = (xdiff * (endY - startY)) / ydiff + startX;
        newStartY = maxy;
        newStartX = endX;
        if (newStartX == minx) {
          newStartX = maxx;
        } else if (newStartX == maxx) {
          newStartX = minx;
        }
      }
      if (endY > maxy) {
        endY = maxy;
        endX = (xdiff * (endY - startY)) / ydiff + startX;
        newStartX = endX;
        newStartY = miny;
        if (newStartX == minx) {
          newStartX = maxx;
        } else if (newStartX == maxx) {
          newStartX = minx;
        }
      }
      if (endX < minx) {
        endX = minx;
        endY = (ydiff * (endX - startX)) / xdiff + startY;
        newStartX = maxx;
        newStartY = endY;
        if (newStartY == miny) {
          newStartY = maxy;
        } else if (newStartY == maxy) {
          newStartY = miny;
        }
      }
      if (endX > maxx) {
        endX = maxx;
        endY = (ydiff * (endX - startX)) / xdiff + startY;
        newStartX = minx;
        newStartY = endY;
        if (newStartY == miny) {
          newStartY = maxy;
        } else if (newStartY == maxy) {
          newStartY = miny;
        }
      }

      topology.drawLine(tg, startX, startY, endX, endY, penSize);

      distX -= (endX - startX);
      distY -= (endY - startY);

      startX = newStartX;
      startY = newStartY;

      count++;
    } while (count < 100 && (StrictMath.abs(distY) >= pixelSize || StrictMath.abs(distX) >= pixelSize));

    markDirty();
  }

  // View settings
  public int fontSize() {
    throw new UnsupportedOperationException();
  }

  public double patchSize() {
    return world.patchSize();
  }

  public double viewWidth() {
    return world.worldWidth();
  }

  public double viewHeight() {
    return world.worldHeight();
  }

  public org.nlogo.api.Perspective perspective() {
    throw new UnsupportedOperationException();
  }

  public double viewOffsetX() {
    throw new UnsupportedOperationException();
  }

  public double viewOffsetY() {
    throw new UnsupportedOperationException();
  }

  public boolean drawSpotlight() {
    throw new UnsupportedOperationException();
  }

  public boolean renderPerspective() {
    return false;
  }

  public WorkspaceContext workspaceContext() {
    throw new UnsupportedOperationException();
  }
}
