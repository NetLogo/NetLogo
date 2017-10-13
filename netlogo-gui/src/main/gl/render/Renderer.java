// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render;

import org.nlogo.api.Agent;
import org.nlogo.api.AgentFollowingPerspective;
import org.nlogo.api.AgentException;
import org.nlogo.api.Drawing3D;
import org.nlogo.api.DrawingInterface;
import org.nlogo.api.Link;
import org.nlogo.api.Patch;
import org.nlogo.api.Patch3D;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.Turtle;
import org.nlogo.api.ViewSettings;
import org.nlogo.api.World;
import org.nlogo.api.WorldWithWorldRenderable;
import org.nlogo.api.World3D;

import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Renderer
    implements GLEventListener {
  final WorldWithWorldRenderable world;
  final ViewSettings renderer;
  private final TurtleRenderer turtleRenderer;
  private final PatchRenderer patchRenderer;
  final WorldRenderer worldRenderer;
  final LinkRenderer linkRenderer;
  final ShapeRenderer shapeRenderer;
  final MouseState mouseState = new MouseState() ;
  final LightManager lightManager = new LightManager();

  int width;
  int height;
  private float ratio;
  NativeSurface surface;

  public static final float WORLD_SCALE = 0.3f;
  public static final float PICK_THRESHOLD = 0.23f;

  /*
  * Collection of all opaque agents that will be rendered each frame. These
  * can be rendered in any order, as long as they are rendered before any
  * transparent objects.
  */
  private final List<Agent> opaqueAgents = new ArrayList<Agent>();

  /*
  * Collection of all partially transparent agents that will be rendered each
  * frame, ordered by their distance from the observer. (Rendering with transparency
  * requires that we sort these objects back to front).
  */
  private final PriorityQueue<Agent> transparentAgents ;

  final GLUgl2 glu = new GLUgl2();
  ShapeManager shapeManager;
  PickListener pickListener;
  ViewInterface pickView;

  // we need to save the last matricies for mouse-x/ycor;
  // also used by context menu - jrn 5/20/05
  private DoubleBuffer modelMatrix;
  private DoubleBuffer projMatrix;
  private IntBuffer viewPort;

  // subclasses/traits should this to false to avoid re-adding lights
  boolean addsLights = true;

  public Renderer(WorldWithWorldRenderable world,
                  ViewSettings graphicsSettings,
                  DrawingInterface drawing,
                  GLViewSettings glSettings) {
    this(world, graphicsSettings, drawing, glSettings, new ShapeRenderer(world));
  }

  public Renderer(WorldWithWorldRenderable world,
                  ViewSettings graphicsSettings,
                  DrawingInterface drawing,
                  GLViewSettings glSettings,
                  ShapeRenderer shapeRenderer) {
    modelMatrix = DoubleBuffer.wrap(new double[16]);
    projMatrix = DoubleBuffer.wrap(new double[16]);
    viewPort = IntBuffer.wrap(new int[4]);

    this.world = world;
    transparentAgents = new PriorityQueue<Agent>(100, new Euclidean(world.observer()));
    renderer = graphicsSettings;
    this.shapeRenderer = shapeRenderer;
    turtleRenderer = createTurtleRenderer(world);
    linkRenderer = createLinkRenderer(world);
    patchRenderer = createPatchRenderer(world, drawing);
    worldRenderer = createWorldRenderer(world, patchRenderer, drawing,
        turtleRenderer, glSettings);
  }

  public Renderer(Renderer glrenderer) {
    world = glrenderer.world;
    transparentAgents = new PriorityQueue<Agent>(100, new Euclidean(world.observer()));
    renderer = glrenderer.renderer;
    worldRenderer = glrenderer.worldRenderer;
    turtleRenderer = glrenderer.turtleRenderer;
    patchRenderer = glrenderer.patchRenderer;
    linkRenderer = glrenderer.linkRenderer;
    shapeRenderer = glrenderer.shapeRenderer;
    shapeManager = glrenderer.shapeManager;
    worldRenderer.shapeManager_$eq(shapeManager);
    shapeRenderer.shapeManager_$eq(shapeManager);
  }

  public void update() {
    worldRenderer.shapeManager_$eq(shapeManager);
    shapeRenderer.shapeManager_$eq(shapeManager);
  }

  @Override
  public void dispose(GLAutoDrawable autoDrawable) {
    surface = null;
  }

  TurtleRenderer createTurtleRenderer(World world) {
    return new TurtleRenderer(world, shapeRenderer);
  }

  WorldRenderer createWorldRenderer(World world, PatchRenderer patchRenderer,
                                    DrawingInterface drawing,
                                    TurtleRenderer turtleRenderer,
                                    GLViewSettings settings) {
    return new WorldRenderer(world, patchRenderer, drawing, turtleRenderer, linkRenderer, settings);
  }

  PatchRenderer createPatchRenderer(World world,
                                    DrawingInterface drawing) {
    return new PatchRenderer(world, drawing, shapeRenderer);
  }

  LinkRenderer createLinkRenderer(World world) {
    return new LinkRenderer(world, shapeRenderer);
  }

  public ExportRenderer createExportRenderer() {
    return new ExportRenderer2D(this);
  }

  public void displayChanged(GLAutoDrawable glDrawable,
                             boolean modeChanged,
                             boolean deviceChanged) {
    surface = glDrawable.getNativeSurface();
  }

  public void init(GLAutoDrawable glDrawable) {
    // Suggestion: If possible, enable any needed features and change any
    // graphics settings as close as possible to the place where they're
    // needed, instead of in here. Other parts of the application might
    // change these settings, which creates hard-to-debug issues.

    GL2 gl = (GL2) glDrawable.getGL();
    surface = glDrawable.getNativeSurface();

    ClassLoader classLoader = getClass().getClassLoader();
    org.nlogo.util.SysInfo.getJOGLInfoString_$eq
        ("JOGL: " + JOGLLoader.getVersion(classLoader));

    org.nlogo.util.SysInfo.getGLInfoString_$eq(
        "OpenGL graphics: " + gl.glGetString(GL2.GL_RENDERER) + "\n"
            + "OpenGL version: " + gl.glGetString(GL2.GL_VERSION) + "\n"
            + "OpenGL vendor: " + gl.glGetString(GL2.GL_VENDOR)
    );

    gl.glShadeModel(GL2.GL_SMOOTH);                     // Enable Smooth Shading
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);          // Black Background
    gl.glClearDepth(1.0f);                            // Depth Buffer Setup
    gl.glEnable(GL2.GL_DEPTH_TEST);              // Enables Depth Testing
    gl.glDepthFunc(GL2.GL_LEQUAL);              // The Type Of Depth Testing To Do

    gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_FASTEST);

    // Lighting
    if (addsLights) {
      lightManager.init(gl);

      Light light1 = new DirectionalLight(new Direction(-1.0f, -0.3f, 0.4f));
      light1.ambient_$eq(new RGBA(0.25f, 0.25f, 0.25f, 1.0f));
      light1.diffuse_$eq(new RGBA(0.35f, 0.35f, 0.35f, 1.0f));
      light1.specular_$eq(new RGBA(0.0f, 0.0f, 0.0f, 0.0f));
      lightManager.addLight(light1);

      Light light2 = new DirectionalLight(new Direction(1.0f, 0.6f, -0.5f));
      light2.ambient_$eq(new RGBA(0.25f, 0.25f, 0.25f, 1.0f));
      light2.diffuse_$eq(new RGBA(0.35f, 0.35f, 0.35f, 1.0f));
      light2.specular_$eq(new RGBA(0.0f, 0.0f, 0.0f, 0.0f));
      lightManager.addLight(light2);
    }

    // This is necessary for properly rendering scaled objects. Without this, small objects
    // may look too bright, and large objects will look flat.
    gl.glEnable(GL2.GL_NORMALIZE);

    // Coloring

    gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
    gl.glEnable(GL2.GL_COLOR_MATERIAL);

    // Remove back-face rendering

    gl.glCullFace(GL2.GL_BACK);
    gl.glEnable(GL2.GL_CULL_FACE);

    // Initialize ShapesManager

    shapeManager = new ShapeManager(gl, glu, world.turtleShapeList(), world.linkShapeList(),
                                    shapeManager == null ? null : shapeManager.customShapes,
                                    this instanceof Renderer3D);
    worldRenderer.init(gl, shapeManager);
    shapeRenderer.shapeManager_$eq(shapeManager);

    // Check for stencil support
    int StencilBits[] = new int[1];
    gl.glGetIntegerv(GL2.GL_STENCIL_BITS, IntBuffer.wrap(StencilBits));
    shapeRenderer.stencilSupport_$eq(StencilBits[0] > 0);
  }

  public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
    GL2 gl = (GL2) glDrawable.getGL();
    surface = glDrawable.getNativeSurface();
    this.width = width;
    this.height = (height > 0) ? height : 1;
    ratio = (float) this.width / (float) this.height;

    mainViewport(gl);
  }

  private void mainViewport(GL2 gl) {
    gl.glViewport(0, 0, width, height);
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();

    // make the z-clip proportional to the max screen edge so the world doesn't
    // just disappear before you can see the whole thing ev 1/15/05
    double zClip = Math.max
        (world.worldWidth(),
            world.worldHeight()) * 4;

    glu.gluPerspective(45.0f, ratio, 0.1, zClip);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  public void display(GLAutoDrawable glDrawable) {
    final GL2 gl = (GL2) glDrawable.getGL();
    surface = glDrawable.getNativeSurface();

    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    shapeManager.checkQueue(gl, glu);

    render(gl);
    gl.glFlush();
  }

  void renderClippingPlane(GL2 gl, double[] eqn, int plane) {
    java.nio.DoubleBuffer eqnBuffer = java.nio.DoubleBuffer.wrap(eqn);
    gl.glClipPlane(plane, eqnBuffer);
    gl.glEnable(plane);
  }

  void setClippingPlanes(GL2 gl) {
    // we get 6 clipping planes guaranteed (0-5).
    // there might be more we can check GL_MAX_CLIPPING_PLANES
    // ev 4/20/06

    // offset the planes a little so they don't cut off the sides of the patches

    renderClippingPlane
        (gl, new double[]
            {1.0f, 0.0, 0.0f, (float) (-(world.minPxcor() - 0.5) * WORLD_SCALE) + 0.01f},
            GL2.GL_CLIP_PLANE0);
    renderClippingPlane
        (gl, new double[]
            {-1.0, 0.0, 0.0, (float) ((world.maxPxcor() + 0.5) * WORLD_SCALE) + 0.01f},
            GL2.GL_CLIP_PLANE1);
    renderClippingPlane
        (gl, new double[]
            {0.0, -1.0, 0.0, (float) ((world.maxPycor() + 0.5) * WORLD_SCALE) + 0.01f},
            GL2.GL_CLIP_PLANE2);
    renderClippingPlane
        (gl, new double[]
            {0.0, 1.0, 0.0, (float) (-(world.minPycor() - 0.5) * WORLD_SCALE) + 0.01f},
            GL2.GL_CLIP_PLANE3);
  }

  void disableClippingPlanes(GL2 gl) {
    gl.glDisable(GL2.GL_CLIP_PLANE0);
    gl.glDisable(GL2.GL_CLIP_PLANE1);
    gl.glDisable(GL2.GL_CLIP_PLANE2);
    gl.glDisable(GL2.GL_CLIP_PLANE3);
    gl.glPopMatrix();
  }

  /**
   * Returns true if this agent should be rendered (i.e., it is not hidden, nor fully transparent,
   * and the observer is not riding this agent). Note: if this agent has a label, the agent will
   * be regarded as "visible".
   */
  boolean agentIsVisible(Agent agent) {
    if (agent instanceof Turtle) {
      Turtle turtle = (Turtle) agent;

      boolean riding_agent = (world.observer().perspective().kind() == PerspectiveJ.RIDE)
          && (world.observer().targetAgent() == turtle);

      return !riding_agent && !turtle.hidden()
        && (turtle.alpha() > 0.0 || turtle.hasLabel());
    } else if (agent instanceof Link) {
      Link link = (Link) agent;
      return !link.hidden() && (link.alpha() > 0.0 || link.hasLabel());
    } else if (agent instanceof Patch3D) {
      // Patch3D supports the alpha variable, so check Patch3D
      // before checking the regular Patch.

      Patch3D patch = (Patch3D) agent;
      return patch.alpha() > 0.0 || patch.hasLabel();
    } else if (agent instanceof Patch) {
      // We will assume all patches are visible. However, perhaps
      // we should only return true for non-black patches.

      return true;
    } else {
      throw new IllegalStateException("Agent must be an instance of Turtle, Patch, or Link.");
    }
  }

  private boolean isPartiallyTransparent(Agent agent) {
    int alpha = agent.alpha();
    return alpha > 0 && alpha < 255;
  }

  void render(GL2 gl) {
    // Notes:
    //
    // This render function only gets called in NetLogo 3D, or in the "3D View" of
    // NetLogo 2D.  (Rendering in NetLogo 2D, without the 3D view, happens in the
    // org.nlogo.render.Renderer class, in the paint() method.)
    //
    // In NetLogo 3D, all turtles are instances of Turtle3D, all patches are instances
    // of Patch3D, and all links are instances of Link3D. Also, world is an instance of
    // World3D, and world.observer() should give you an instance of Observer3D. Also,
    // beware of the fact that the Renderer3D class overrides some methods (e.g.,
    // renderWorld) when we're in NetLogo 3D - the overriden versions of these methods
    // will get called. However, since render() has not been overriden in Renderer3D,
    // this method still gets called.
    //
    // In the 3D view in 2D, all turtles are instances of Turtle, all patches are
    // instances of Patch, and... you get the idea. Note, however, that the non-3D
    // version of Observer still has a zcor attribute since the camera is free to
    // move about in three dimensions in the 3D view in 2D. Also, none of the
    // overrides in the Renderer3D class have any effect when we're in the 3D view
    // in 2D, so the methods that get called are the ones in this class (e.g., when
    // we call renderWorld() below, the method that gets called is
    // Renderer.renderWorld() instead of Renderer3D.renderWorld() when we're in
    // the 3D view in 2D.)
    //
    // In the code below, when we are checking if something is an instance of Turtle3D,
    // Patch3D, or Link3D, this signifies something must only be performed in
    // NetLogo 3D, and not in the 3D view in 2D. We try to minimize such cases since
    // we want this function to remain as general as possible.
    //
    // On the other hand, if we are checking if something is an instance of Turtle,
    // Patch, or Link (without the 3D suffix), this signifies something that is
    // performed in both NetLogo 3D, and in the 3D view in 2D because Turtle3D,
    // Patch3D, and Link3D extend Turtle, Patch, and Link (and thus, all Turtle3D
    // instances are also Turtle instances, and so on).
    //
    // Here are some subtleties: In NetLogo 3D, patches get rendered when you call
    // worldRenderer.renderPatchShapes(), via the following pathway:
    //     Renderer.render() (this method) calls:
    //     WorldRenderer3D.renderPatchShapes(), which calls:
    //     PatchRenderer3D.renderPatches()           <-- Patches get rendered here
    //
    // However, when you're in the 3D view in 2D, patches get rendered when you
    // call renderWorld(), via the following pathway:
    //     Renderer.render() (this method) calls:
    //     Renderer.renderWorld(), which calls:
    //     WorldRenderer.renderWorld(), which calls:
    //     PatchRenderer.renderPatches()             <-- Patches get rendered here
    //
    // This might lead you to believe that patches are getting rendered twice,
    // because we're calling renderWorld(), and then later we're calling
    // worldRenderer.renderPatchShapes(). However, this is not the case because:
    //
    //   1. WorldRenderer.renderPatchShapes(), unlike
    //      WorldRenderer3D.renderPatchShapes(), does not call
    //      patchRenderer.renderPatches(), so patches DO NOT get rendered when
    //      we call worldRenderer.renderPatchShapes() when we're in the 3D
    //      view in 2D.
    //   2. WorldRenderer3D.renderWorld(), unlike WorldRenderer.renderWorld(),
    //      does not call patchRenderer.renderPatches(), so patches do NOT
    //      get rendered when we call renderWorld() when we're in NetLogo 3D.
    //
    // This is a bit confusing since functions that have the same name are
    // doing drastically different things depending on whether we're in true
    // 3D or if we're in the 3D view in 2D. I might get around to fixing this
    // later, but I'm afraid that might be a big task, so for now I'm going
    // to leave this comment in an attempt to clarify what's going on.
    //

    gl.glLoadIdentity();

    synchronized (world) {
      Perspective perspective = world.observer().perspective();
      Agent targetAgent = world.observer().targetAgent();

      worldRenderer.observePerspective(gl);

      worldRenderer.renderCrossHairs(gl);

      if (addsLights) {
        lightManager.applyLighting();
      }

      // Uncomment the code below to show the positions and directions of all the lights
      // in the world (only works in NetLogo 3D, not the 3D view in 2D).
      /*
      if (world instanceof World3D)
      {
        double observerDistance = Math.sqrt(world.observer().oxcor() * world.observer().oxcor()
              + world.observer().oycor() * world.observer().oycor()
              + world.observer().ozcor() * world.observer().ozcor());
        lightManager.showLights(glu, (World3D)world, WORLD_SCALE, observerDistance, shapeRenderer);
      }
      */

      renderWorld(gl, world);

      setClippingPlanes(gl);

      // Calculate the line scale to use for rendering outlined turtles and links,
      // as well as link stamps.
      double lineScale = calculateLineScale();

      boolean sortingNeeded = world.mayHavePartiallyTransparentObjects();

      if (!sortingNeeded) {
        worldRenderer.renderPatchShapes
            (gl, outlineAgent, renderer.fontSize(), renderer.patchSize());

        linkRenderer.renderLinks(gl, glu, renderer.fontSize(), renderer.patchSize(), outlineAgent);

        turtleRenderer.renderTurtles(gl, glu, renderer.fontSize(), renderer.patchSize(), outlineAgent);

        // Render stamps and trails
        //
        // Note: in the 3D view in 2D, stamps and trails appear as bitmaps
        // on the drawing layer, which already got rendered in renderWorld().
        //
        // In the true 3D version, we skipped the call to renderDrawing()
        // because there's the possibility that stamps and trails might be
        // transparent, and so they need to get sorted along with the rest
        // of the objects in the scene.
        //
        // However, since we've determined that no sorting is needed in this
        // block, we can safely render the drawing layer now.

        if (world instanceof World3D) {
          worldRenderer.renderDrawing(gl);
        }
      } else {
        // Make sure everything needed for transparency is enabled.
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        opaqueAgents.clear();
        transparentAgents.clear();

        for (Agent agent : world.turtles().agents()) {
          if (agentIsVisible(agent)) {
            if (isPartiallyTransparent(agent)) {
              transparentAgents.add(agent);
            } else {
              opaqueAgents.add(agent);
            }
          }
        }

        for (Agent agent : world.patches().agents()) {
          if (agentIsVisible(agent)) {
            if (isPartiallyTransparent(agent)) {
              transparentAgents.add(agent);
            } else {
              opaqueAgents.add(agent);
            }
          }
        }

        for (Agent agent : world.links().agents()) {
          if (agentIsVisible(agent)) {
            if (isPartiallyTransparent(agent)) {
              transparentAgents.add(agent);
            } else {
              opaqueAgents.add(agent);
            }
          }
        }

        // Now add stamps and trails.
        //
        // Note: in the 3D view in 2D, stamps and trails appear as bitmaps
        // on the drawing layer, which already got rendered in renderWorld().
        // We only have to worry about the true 3D version.

        if (world instanceof World3D) {
          // Link stamps
          for (org.nlogo.api.Link stamp : ((Drawing3D) world.getDrawing()).linkStamps()) {
            if (agentIsVisible(stamp)) {
              if (isPartiallyTransparent(stamp)) {
                transparentAgents.add(stamp);
              } else {
                opaqueAgents.add(stamp);
              }
            }
          }

          // Turtle stamps
          for (org.nlogo.api.Turtle stamp : ((Drawing3D) world.getDrawing()).turtleStamps()) {
            if (agentIsVisible(stamp)) {
              if (isPartiallyTransparent(stamp)) {
                transparentAgents.add(stamp);
              } else {
                opaqueAgents.add(stamp);
              }
            }
          }

          // Turtle trails
          ((WorldRenderer3D) worldRenderer).renderTrails(gl);

          // Note: We are currently not supporting transparent turtle trails
          // (trails are left by the turtles when you use the pen-down command).
          // The difficulty with these is that they are not instances of Agent,
          // so we would have to reimplement our code to use Objects instead,
          // or create some sort of Renderable interface (but this would create
          // more problems because we would need to make Agent implement
          // this Renderable interface, so either we have to create a bad dependency
          // from the org.nlogo.api package to the render package, or else we
          // have to put the Renderable interface in the org.nlogo.api package).
          // There may also be a significant performance issue since the trails
          // consist of a large number of small line segments, all of which would
          // need to be sorted each frame for transparency to work properly.
          //
          // For reference:
          // for( org.nlogo.api.DrawingLine3D line : ( (Drawing3D) world.getDrawing() ).lines() )
          // {
          //     // add trail segment to the list here
          // }
        }

        //    System.out.printf( "\t\t%d opaque objects.\n" , opaqueAgents.size() ) ;
        //    System.out.printf( "\t\t%d transparent objects (sorted).\n" , transparentAgents.size() ) ;

        // Render the opaque objects first
        for (Agent agent : opaqueAgents) {
          renderAgent(gl, agent, lineScale);
        }

        // Now render the transparent agents in sorted order (back to front)
        while (!transparentAgents.isEmpty()) {
          Agent agent = transparentAgents.remove();
          renderAgent(gl, agent, lineScale);
        }

        gl.glDisable(GL2.GL_BLEND);
      }

      if (outlineAgent instanceof org.nlogo.api.Patch) {
        patchRenderer.renderOutline(gl, (org.nlogo.api.Patch) outlineAgent);
      }

      disableClippingPlanes(gl);

      //
      // Highlight agents that are being watched or followed.
      //
      if (targetAgent instanceof Turtle) {
        turtleRenderer.renderHighlight(gl, (Turtle) targetAgent);
      } else if (targetAgent instanceof org.nlogo.api.Patch) {
        patchRenderer.renderHightlight(gl, (Patch) targetAgent);
      }

      if (mouseState.on() || mouseState.pickRequest()) {
        storeMatricies(gl);

        if (mouseState.pickRequest()) {
          performPick();
        }

        if ((perspective.kind() != PerspectiveJ.OBSERVE)
            && mouseState.inside() && (mouseState.point() != null)) {
          updateMouseCors();
        }
      }
    }
  }

  void renderAgent(GL2 gl, Agent agent, Double lineScale) {
    if (agent instanceof Turtle) {
      turtleRenderer.renderWrappedTurtle(gl, (Turtle) agent, renderer.fontSize(),
          renderer.patchSize(), (agent == outlineAgent), lineScale);
    } else if (agent instanceof org.nlogo.api.Patch3D) {
      worldRenderer.renderIndividualPatchShapes(gl, (org.nlogo.api.Patch3D) agent, outlineAgent,
          renderer.fontSize(), renderer.patchSize());
    } else if (agent instanceof org.nlogo.api.Link) {
      linkRenderer.renderIndividualLinks(gl, glu, (Link) agent,
          renderer.fontSize(), renderer.patchSize(), outlineAgent);
    }

    // Note: If we are in the 3D view in 2D, patches have already been
    // rendered back when we called renderWorld() above. Thus, we do not
    // have to check if( agent instanceof Patch ).
  }

  void renderWorld(GL2 gl, World world) {
    // This version of renderWorld gets called when we're in the 3D view in 2D.
    // For NetLogo 3D, look in Renderer3D.renderWorld().
    //
    // In the 3D view in 2D, worldRenderer.renderWorld() will cause the patches
    // to get rendered. worldRenderer.renderDrawing() will cause any image
    // that was imported using the import-drawing command to get rendered, along
    // with stamps and trails.
    //
    // Patches and images are guaranteed to be opaque in the 3D view in 2D, so
    // there's no problem with rendering these here (opaque objects must be
    // rendered first, and any transparent objects afterward).
    //
    // However, we might have some problems with stamps/trails in the 3D view
    // in 2D... EDIT: Actually, when you stamp things in the 3D view in 2D, their
    // stamps become the "2D" versions of the shapes. Kind of a quirky feature,
    // but it does mean that rendering stamps/trails in here should not be a problem,
    // even if they are transparent, because they are guaranteed to never
    // obscure any transparent 3D turtles/links.

    gl.glPushMatrix();
    translateWorld(gl, world);

    worldRenderer.renderWorld(gl, renderer.fontSize(), renderer.patchSize());

    worldRenderer.renderDrawing(gl);

    gl.glPopMatrix();
  }

  /*
  * Calculates the line scale to use for rendering outlined turtles and links.
  */
  double calculateLineScale() {
    double distance;

    Perspective p = world.observer().perspective();

    if (p instanceof AgentFollowingPerspective) {
      distance = ((AgentFollowingPerspective) p).followDistance();
    } else {
      distance = world.observer().orientation().get().dist();
    }

    if (distance != 0) {
      double width = world.worldWidth();
      double height = world.worldHeight();

      double maxDimension = Math.max(width, height);

      if (world instanceof World3D) {
        double depth = ((World3D) world).worldDepth();
        maxDimension = Math.max(maxDimension, depth);
      }

      return 1.5 * maxDimension / distance;
    } else {
      return 0.0;
    }
  }

  private Agent outlineAgent = null;

  public void outlineAgent(Agent agent) {
    outlineAgent = agent;
  }

  public void cleanUp() {
    worldRenderer.cleanUp();
  }

  /// Picking

  // queue a request for object selection/pick
  public void queuePick(java.awt.Point mousePt, PickListener pickListener, ViewInterface view) {
    mouseState.pickRequest_$eq(true);
    mouseState.point_$eq(mousePt);
    this.pickListener = pickListener;
    this.pickView = view;
  }

  // pick/select objects for context menu
  void performPick() {
    List<Agent> agents = new ArrayList<Agent>(5);
    int toSurfacePixels[] = { (int) mouseState.point().getX(), (int) mouseState.point().getY() };
    surface.convertToPixelUnits(toSurfacePixels);
    double[][] ray = generatePickRay(toSurfacePixels[0], (height - toSurfacePixels[1]));
    pickPatches(agents, ray);
    pickTurtles(agents, ray);
    pickLinks(agents, ray);
    pickListener.pick(mouseState.point(), agents, pickView);
    mouseState.pickRequest_$eq(false);
  }

  // saves current transformation matricies and viewport
  private void storeMatricies(GL2 gl) {
    gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelMatrix);
    gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projMatrix);
    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewPort);
  }

  // generates a pick/selection ray from mouse coordinates
  double[][] generatePickRay(double mouseX, double mouseY) {
    double[][] ray = new double[2][3];

    // create pick-ray
    glu.gluUnProject(mouseX, mouseY, 0.0d, modelMatrix, projMatrix,
        viewPort, DoubleBuffer.wrap(ray[0]));
    glu.gluUnProject(mouseX, mouseY, 1.0d, modelMatrix, projMatrix,
        viewPort, DoubleBuffer.wrap(ray[1]));

    return ray;
  }

  double wrapX(double x) {
    try {
      return world.wrapX(x);
    } catch (AgentException e) {
      return x;
    }
  }

  double wrapY(double y) {
    try {
      return world.wrapY(y);
    } catch (AgentException e) {
      return y;
    }
  }

  // detects which patch a pick-ray intersects with
  void pickPatches(List<Agent> agents, double[][] ray) {
    // detect any patches in the pick-ray ( ( Renderer.WORLD_SCALE / 2 )
    // is the offset of the patches plane in the z-axis - jrn)
    double scale = (1.0 / WORLD_SCALE);
    double t = ((ray[0][2] + (Renderer.WORLD_SCALE / 2.0)) /
        (ray[0][2] - ray[1][2]));
    double xi = scale * (ray[0][0] + (ray[1][0] - ray[0][0]) * t);
    double yi = scale * (ray[0][1] + (ray[1][1] - ray[0][1]) * t);

    if ((xi < world.maxPxcor() + 0.5) && (xi >= world.minPxcor() - 0.5) &&
        (yi < world.maxPycor() + 0.5) && (yi >= world.minPycor() - 0.5)) {
      xi = wrapX(xi + world.followOffsetX());
      yi = wrapY(yi + world.followOffsetY());

      mouseState.xcor_$eq(xi);
      mouseState.ycor_$eq(yi);

      if (agents != null) {
        try {
          Patch patch = world.getPatchAt(xi, yi);
          agents.add(patch);
        } catch (AgentException e) {
          org.nlogo.api.Exceptions.ignore(e);
        }
      }
    }
  }

  // detects which turtle(s) a pick-ray intersects with
  void pickTurtles(List<Agent> agents, double[][] ray) {
    // detect any turtles in the pick-ray
    for (Agent a : world.turtles().agents()) {
      Turtle turtle = (Turtle) a;
      if (!turtle.hidden()) {
        double size = turtle.size();

        double[] coord = getTurtleCoords(turtle, size);

        // determining distance to pick ray
        double ux = ray[1][0] - ray[0][0];
        double uy = ray[1][1] - ray[0][1];
        double uz = ray[1][2] - ray[0][2];
        double vx = ray[0][0] - coord[0];
        double vy = ray[0][1] - coord[1];
        double vz = ray[0][2] - coord[2];

        double crossX = (uy * vz - uz * vy);
        double crossY = (uz * vx - ux * vz);
        double crossZ = (ux * vy - uy * vx);
        double lengt = Math.sqrt((crossX * crossX) +
            (crossY * crossY) + (crossZ * crossZ));
        double lengb = Math.sqrt((ux * ux) + (uy * uy) +
            (uz * uz));

        double dist = (lengt / lengb);

        if (dist <= size * PICK_THRESHOLD) {
          agents.add(turtle);
        }
      }
    }
  }

  // detects which turtle(s) a pick-ray intersects with
  void pickLinks(List<Agent> agents, double[][] ray) {
    // detect any turtles in the pick-ray
    for (Agent a : world.links().agents()) {
      Link link = (Link) a;
      if (!link.hidden()) {
        double[] end1 = getTurtleCoords(link.end1(), 0);
        double[] end2 = getTurtleCoords(link.end2(), 0);

        double dist = Picker.distanceFromRayToSegment(ray, end1, end2);

        if (dist <= PICK_THRESHOLD) {
          agents.add(link);
        }
      }
    }
  }

  double[] getTurtleCoords(Turtle turtle, double height) {
    return new double[]{world.wrappedObserverX(turtle.xcor()) * Renderer.WORLD_SCALE,
        world.wrappedObserverY(turtle.ycor()) * Renderer.WORLD_SCALE, 0};
  }


  /// Mouse interaction

  public void setMouseMode(boolean mode) {
    mouseState.on_$eq(mode);
  }

  public double mouseXCor() {
    return mouseState.xcor();
  }

  public double mouseYCor() {
    return mouseState.ycor();
  }

  public boolean mouseDown() {
    return mouseState.down();
  }

  public void mouseDown(boolean mouseDown) {
    mouseState.down_$eq(mouseDown);
  }

  public boolean mouseInside() {
    return mouseState.inside();
  }

  public void resetMouseCors() {
    mouseState.xcor_$eq(0);
    mouseState.ycor_$eq(0);
  }

  public void setMouseCors(java.awt.Point mousePt) {
    double[][] ray = generatePickRay(mousePt.getX(), (height - mousePt.getY()));
    pickPatches(null, ray);
    mouseState.point_$eq(mousePt);
  }

  public void updateMouseCors() {
    double[][] ray = generatePickRay(mouseState.point().getX(), (height - mouseState.point().getY()));
    pickPatches(null, ray);
  }

  public void mouseInside(double mx, double my) {
    double[][] ray = generatePickRay(mx, height - my);
    double scale = (1 / WORLD_SCALE);
    double t = ((ray[0][2] + (Renderer.WORLD_SCALE / 2)) /
        (ray[0][2] - ray[1][2]));
    double xi = scale * (ray[0][0] + (ray[1][0] - ray[0][0]) * t);
    double yi = scale * (ray[0][1] + (ray[1][1] - ray[0][1]) * t);

    mouseState.inside_$eq(
      (xi < world.maxPxcor() + 0.5) && (xi >= world.minPxcor() - 0.5) &&
      (yi < world.maxPycor() + 0.5) && (yi >= world.minPycor() - 0.5));
  }

  /// Crosshairs

  public void showCrossHairs(boolean visible) {
    worldRenderer.showCrossHairs_$eq(visible);
  }

  /// Shapes

  public void addCustomShapes(String filename)
      throws java.io.IOException,
      org.nlogo.shape.InvalidShapeDescriptionException {
    shapeManager.addCustomShapes(filename);
  }

  public void invalidateTurtleShape(String shape) {
    shapeManager.invalidateTurtleShape(shape);
  }

  public void invalidateLinkShape(String shape) {
    shapeManager.invalidateLinkShape(shape);
  }

  public void translateWorld(GL2 gl, World world) {
    gl.glTranslated
        ((world.maxPxcor() + world.minPxcor()) / 2.0 * Renderer.WORLD_SCALE,
            (world.maxPycor() + world.minPycor()) / 2.0 * Renderer.WORLD_SCALE,
            0.0);
  }
}
