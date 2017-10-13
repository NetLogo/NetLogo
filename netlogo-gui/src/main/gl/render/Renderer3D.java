// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render;

import org.nlogo.api.Agent;
import org.nlogo.api.AgentException;
import org.nlogo.api.DrawingInterface;
import org.nlogo.api.Patch3D;
import org.nlogo.api.Turtle;
import org.nlogo.api.Turtle3D;
import org.nlogo.api.ViewSettings;
import org.nlogo.api.World;
import org.nlogo.api.World3D;
import org.nlogo.api.WorldWithWorldRenderable;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.List;

public class Renderer3D
    extends org.nlogo.gl.render.Renderer {
  public Renderer3D(WorldWithWorldRenderable world,
                    ViewSettings graphicsSettings,
                    DrawingInterface drawing,
                    GLViewSettings glSettings) {
    super(world, graphicsSettings, drawing, glSettings, new ShapeRenderer3D((World3D) world));
  }

  public Renderer3D(Renderer glrenderer) {
    super(glrenderer);
  }

  @Override
  void setClippingPlanes(GL2 gl) {
    super.setClippingPlanes(gl);
    World3D w = (World3D) world;

    // don't clip in the z-direction with 2D models.
    if (w.worldDepth() > 1) {
      // offset the planes ever so slightly so they don't cut off the tops of the patches
      renderClippingPlane
          (gl, new double[]
              {0.0f, 0.0, 1.0f, (float) (-(w.minPzcor() - 0.5) * WORLD_SCALE) + 0.01f},
              GL2.GL_CLIP_PLANE4);
      renderClippingPlane
          (gl, new double[]
              {0.0, 0.0, -1.0, (float) ((w.maxPzcor() + 0.5) * WORLD_SCALE) + 0.01f},
              GL2.GL_CLIP_PLANE5);
    }
  }

  @Override
  void disableClippingPlanes(GL2 gl) {
    super.disableClippingPlanes(gl);
    gl.glDisable(GL2.GL_CLIP_PLANE4);
    gl.glDisable(GL2.GL_CLIP_PLANE5);
  }

  @Override
  TurtleRenderer createTurtleRenderer(World world) {
    return new TurtleRenderer3D(world, shapeRenderer);
  }

  @Override
  WorldRenderer createWorldRenderer(World world,
                                    PatchRenderer patchRenderer,
                                    DrawingInterface drawing,
                                    TurtleRenderer turtleRenderer,
                                    GLViewSettings settings) {
    return new WorldRenderer3D((World3D) world, (PatchRenderer3D) patchRenderer,
        drawing, (TurtleRenderer3D) turtleRenderer, (LinkRenderer3D) linkRenderer, settings);
  }

  @Override
  PatchRenderer createPatchRenderer(World world,
                                    DrawingInterface drawing) {
    return new PatchRenderer3D((World3D) world, drawing, shapeRenderer);
  }

  @Override
  LinkRenderer createLinkRenderer(World world) {
    return new LinkRenderer3D(world, shapeRenderer);
  }

  @Override
  public ExportRenderer createExportRenderer() {
    return new ExportRenderer3D(this);
  }

  @Override
  void renderWorld(GL2 gl, World world) {
    gl.glPushMatrix();
    translateWorld(gl, world);

    worldRenderer.renderWorld(gl, renderer.fontSize(), renderer.patchSize());

    gl.glPopMatrix();

    // In the 3D version, we'll render the drawing layer directly from Renderer.render().
    // worldRenderer.renderDrawing( gl ) ;
  }

  @Override
  public void translateWorld(GL2 gl, World world) {
    World3D w = (World3D) world;

    gl.glTranslated
        (((world.maxPxcor() + world.minPxcor()) / 2.0) * Renderer.WORLD_SCALE,
            ((world.maxPycor() + world.minPycor()) / 2.0) * Renderer.WORLD_SCALE,
            ((w.maxPzcor() + w.minPzcor()) / 2.0) * Renderer.WORLD_SCALE);
  }

  @Override
  double[] getTurtleCoords(Turtle turtle, double height) {
    Turtle3D t = (Turtle3D) turtle;
    double[] coords = new double[]{t.xcor(), t.ycor(), t.zcor()};
    coords[0] = world.wrappedObserverX(coords[0]) * Renderer.WORLD_SCALE;
    coords[1] = world.wrappedObserverY(coords[1]) * Renderer.WORLD_SCALE;
    coords[2] = ((World3D) world).wrappedObserverZ(coords[2]) * Renderer.WORLD_SCALE;

    return coords;
  }

  double[] getPatchCoords(Patch3D patch) {
    double[] coords = new double[]{patch.pxcor(), patch.pycor(), patch.pzcor()};
    coords[0] = world.wrappedObserverX(coords[0]) * Renderer.WORLD_SCALE;
    coords[1] = world.wrappedObserverY(coords[1]) * Renderer.WORLD_SCALE;
    coords[2] = ((World3D) world).wrappedObserverZ(coords[2]) * Renderer.WORLD_SCALE;

    return coords;
  }

  // pick/select objects for context menu
  @Override
  void performPick() {
    List<Agent> agents = new ArrayList<Agent>();
    int toSurfacePixels[] = { (int) mouseState.point().getX(), (int) mouseState.point().getY() };
    surface.convertToPixelUnits(toSurfacePixels);
    double[][] ray = generatePickRay(toSurfacePixels[0], (height - toSurfacePixels[1]));
    pickTurtles(agents, ray);
    pickLinks(agents, ray);
    pickPatches(agents, ray);
    pickListener.pick(mouseState.point(), agents, pickView);
    mouseState.pickRequest_$eq(false);
  }

  double wrapZ(double z) {
    return ((World3D) world).wrapZ(z);
  }

  @Override
  void pickPatches(List<Agent> agents, double[][] ray) {
    if (agents == null) {
      return;
    }

    World3D w = (World3D) world;

    // detect any patches in the pick-ray ( ( Renderer.WORLD_SCALE / 2 )
    // is the offset of the patches plane in the z-axis - jrn)
    double scale = (1 / WORLD_SCALE);
    double deltaz = Math.abs(ray[0][2] - ray[1][2]);
    double deltay = Math.abs(ray[0][1] - ray[1][1]);
    double deltax = Math.abs(ray[0][0] - ray[1][0]);

    double xi = ray[0][0] * scale;
    double yi = ray[0][1] * scale;
    double zi = ray[0][2] * scale;
    double xinc = 0;
    double yinc = 0;
    double zinc = 0;
    double t = 0;
    double min = 0;
    double max = 0;

    if (deltaz >= deltay && deltaz >= deltax) {
      zinc = 1;
      t = 1 / (ray[1][2] - ray[0][2]);
      xinc = (ray[1][0] - ray[0][0]) * t;
      yinc = (ray[1][1] - ray[0][1]) * t;
      min = w.minPzcor();
      max = w.maxPzcor();
      zi = min;
      xi -= xinc * ray[0][2] * scale - (min * xinc);
      yi -= yinc * ray[0][2] * scale - (min * yinc);
    } else if (deltay >= deltax) {
      yinc = 1;
      t = 1 / (ray[1][1] - ray[0][1]);
      xinc = (ray[1][0] - ray[0][0]) * t;
      zinc = (ray[1][2] - ray[0][2]) * t;
      min = w.minPycor();
      max = w.maxPycor();
      yi = min;
      xi -= xinc * ray[0][1] * scale - (min * xinc);
      zi -= zinc * ray[0][1] * scale - (min * zinc);
    } else {
      xinc = 1;
      t = -1 / (ray[0][0] - ray[1][0]);
      yinc = (ray[1][1] - ray[0][1]) * t;
      zinc = (ray[1][2] - ray[0][2]) * t;
      min = w.minPxcor();
      max = w.maxPxcor();
      xi = min;
      yi -= yinc * ray[0][0] * scale - (min * yinc);
      zi -= zinc * ray[0][0] * scale - (min * zinc);
    }

    for (double c = min; c <= max; c++) {
      double x = xi;
      double y = yi;
      double z = zi;

      if ((x < world.maxPxcor() + 0.5) && (x >= world.minPxcor() - 0.5) &&
          (y < world.maxPycor() + 0.5) && (y >= world.minPycor() - 0.5) &&
          (z < w.maxPzcor() + 0.5) && (z >= w.minPzcor() - 0.5)) {
        try {
          agents.add(w.getPatchAt(wrapX(x + w.followOffsetX()),
              wrapY(y + w.followOffsetY()),
              wrapZ(z + w.followOffsetZ())));
        } catch (AgentException e) {
          org.nlogo.api.Exceptions.ignore(e);
        }
      }
      xi += xinc;
      yi += yinc;
      zi += zinc;
    }
  }
}
