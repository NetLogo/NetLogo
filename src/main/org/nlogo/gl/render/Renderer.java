package org.nlogo.gl.render;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import org.nlogo.api.Agent;
import org.nlogo.api.AgentException;
import org.nlogo.api.Turtle;
import org.nlogo.api.TurtleStamp3D;
import org.nlogo.api.Patch;
import org.nlogo.api.Patch3D;
import org.nlogo.api.Link;
import org.nlogo.api.Link3D;
import org.nlogo.api.LinkStamp3D;
import org.nlogo.api.Drawing3D;
import org.nlogo.api.Vect;
import org.nlogo.api.World;
import org.nlogo.api.World3D;
import org.nlogo.api.ViewSettings;
import org.nlogo.api.DrawingInterface;
import org.nlogo.api.Perspective;
import org.nlogo.api.ShapeList;

public class Renderer
    implements GLEventListener {
  final World world;
  final ViewSettings renderer;
  private final TurtleRenderer turtleRenderer;
  private final PatchRenderer patchRenderer;
  final WorldRenderer worldRenderer;
  final LinkRenderer linkRenderer;
  final ShapeRenderer shapeRenderer;

  int width;
  int height;
  private float ratio;

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

  final GLU glu = new GLU();

  ShapeManager shapeManager;

  // current mouse coordinates/status
  private boolean mouseOn;
  private boolean mouseInside;
  private double mouseXCor;
  private double mouseYCor;
  private boolean mouseDown;

  // object pick/selection request for context menu
  boolean pickRequest;
  java.awt.Point mousePt;
  PickListener pickListener;

  // we need to save the last matricies for mouse-x/ycor;
  // also used by context menu - jrn 5/20/05
  private DoubleBuffer modelMatrix;
  private DoubleBuffer projMatrix;
  private IntBuffer viewPort;

  public Renderer(World world,
                  ViewSettings graphicsSettings,
                  DrawingInterface drawing,
                  GLViewSettings glSettings) {
    this(world, graphicsSettings, drawing, glSettings, new ShapeRenderer(world));
  }

  public Renderer(World world,
                  ViewSettings graphicsSettings,
                  DrawingInterface drawing,
                  GLViewSettings glSettings,
                  ShapeRenderer shapeRenderer) {
    mouseOn = false;
    mouseInside = false;
    pickRequest = false;
    mousePt = null;
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

  ShapeManager createShapeManager(GL gl, GLU glu, ShapeList turtleShapeList,
                                  ShapeList linkShapeList,
                                  Map<String, List<String>> customShapes) {
    return new ShapeManager(gl, glu, turtleShapeList, linkShapeList, customShapes);
  }

  public ExportRenderer createExportRenderer() {
    return new ExportRenderer2D(this);
  }

  public void displayChanged(GLAutoDrawable gLDrawable,
                             boolean modeChanged,
                             boolean deviceChanged) {
  }

  public void init(GLAutoDrawable gLDrawable) {
    // Suggestion: If possible, enable any needed features and change any
    // graphics settings as close as possible to the place where they're
    // needed, instead of in here. Other parts of the application might
    // change these settings, which creates hard-to-debug issues.

    GL gl = gLDrawable.getGL();

    ClassLoader classLoader = getClass().getClassLoader();
    org.nlogo.util.SysInfo.getJOGLInfoString_$eq
        ("JOGL: " + JOGLLoader.getVersion(classLoader));

    org.nlogo.util.SysInfo.getGLInfoString_$eq(
        "OpenGL graphics: " + gl.glGetString(GL.GL_RENDERER) + "\n"
            + "OpenGL version: " + gl.glGetString(GL.GL_VERSION) + "\n"
            + "OpenGL vendor: " + gl.glGetString(GL.GL_VENDOR)
    );

    gl.glShadeModel(GL.GL_SMOOTH);                     // Enable Smooth Shading
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);          // Black Background
    gl.glClearDepth(1.0f);                            // Depth Buffer Setup
    gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
    gl.glDepthFunc(GL.GL_LEQUAL);              // The Type Of Depth Testing To Do

    gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);

    // Lighting

    float[] lightAmbient = {0.25f, 0.25f, 0.25f, 1.0f};
    float[] lightDiffuse = {0.35f, 0.35f, 0.35f, 1.0f};

    gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, FloatBuffer.wrap(lightAmbient));
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, FloatBuffer.wrap(lightDiffuse));
    gl.glEnable(GL.GL_LIGHT0);

    float[] lightAmbient2 = {0.25f, 0.25f, 0.25f, 1.0f};
    float[] lightDiffuse2 = {0.35f, 0.35f, 0.35f, 1.0f};
    float[] lightPos2 = {0.25f, 0.25f, 0.25f, 0.0f};
    gl.glLightfv(GL.GL_LIGHT2, GL.GL_AMBIENT, FloatBuffer.wrap(lightAmbient2));
    gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, FloatBuffer.wrap(lightDiffuse2));
    gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, FloatBuffer.wrap(lightPos2));
    gl.glEnable(GL.GL_LIGHT2);

    gl.glEnable(GL.GL_LIGHTING);

    // Coloring

    gl.glColorMaterial(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE);
    gl.glEnable(GL.GL_COLOR_MATERIAL);

    // Remove back-face rendering

    gl.glCullFace(GL.GL_BACK);
    gl.glEnable(GL.GL_CULL_FACE);

    // Initalize ShapesManager

    if (shapeManager == null) {
      shapeManager = createShapeManager(gl, glu, world.turtleShapeList(),
          world.linkShapeList(), null);
    } else {
      shapeManager = createShapeManager(gl, glu, world.turtleShapeList(), world.linkShapeList(),
          shapeManager.customShapes);
    }

    worldRenderer.init(gl, shapeManager);
    shapeRenderer.shapeManager_$eq(shapeManager);

    // Check for stencil support
    int StencilBits[] = new int[1];
    gl.glGetIntegerv(GL.GL_STENCIL_BITS, IntBuffer.wrap(StencilBits));
    shapeRenderer.stencilSupport_$eq(StencilBits[0] > 0);
  }

  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
    GL gl = gLDrawable.getGL();
    this.width = width;
    this.height = (height > 0) ? height : 1;
    ratio = (float) this.width / (float) this.height;

    mainViewport(gl);
  }

  private void mainViewport(GL gl) {
    gl.glViewport(0, 0, width, height);
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();

    // make the z-clip proportional to the max screen edge so the world doesn't
    // just disappear before you can see the whole thing ev 1/15/05
    double zClip = Math.max
        (world.worldWidth(),
            world.worldHeight()) * 4;

    glu.gluPerspective(45.0f, ratio, 0.1, zClip);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  public void display(GLAutoDrawable gLDrawable) {
    final GL gl = gLDrawable.getGL();

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    shapeManager.checkQueue(gl, glu);

    render(gl);
    gl.glFlush();
  }

  void renderClippingPlane(GL gl, double[] eqn, int plane) {
    java.nio.DoubleBuffer eqnBuffer = java.nio.DoubleBuffer.wrap(eqn);
    gl.glClipPlane(plane, eqnBuffer);
    gl.glEnable(plane);
  }

  void setClippingPlanes(GL gl) {
    // we get 6 clipping planes guaranteed (0-5).
    // there might be more we can check GL_MAX_CLIPPING_PLANES
    // ev 4/20/06

    // offset the planes a little so they don't cut off the sides of the patches

    renderClippingPlane
        (gl, new double[]
            {1.0f, 0.0, 0.0f, (float) (-(world.minPxcor() - 0.5) * WORLD_SCALE) + 0.01f},
            GL.GL_CLIP_PLANE0);
    renderClippingPlane
        (gl, new double[]
            {-1.0, 0.0, 0.0, (float) ((world.maxPxcor() + 0.5) * WORLD_SCALE) + 0.01f},
            GL.GL_CLIP_PLANE1);
    renderClippingPlane
        (gl, new double[]
            {0.0, -1.0, 0.0, (float) ((world.maxPycor() + 0.5) * WORLD_SCALE) + 0.01f},
            GL.GL_CLIP_PLANE2);
    renderClippingPlane
        (gl, new double[]
            {0.0, 1.0, 0.0, (float) (-(world.minPycor() - 0.5) * WORLD_SCALE) + 0.01f},
            GL.GL_CLIP_PLANE3);
  }

  void disableClippingPlanes(GL gl) {
    gl.glDisable(GL.GL_CLIP_PLANE0);
    gl.glDisable(GL.GL_CLIP_PLANE1);
    gl.glDisable(GL.GL_CLIP_PLANE2);
    gl.glDisable(GL.GL_CLIP_PLANE3);
    gl.glPopMatrix();
  }

  /**
   * If there is at least one partially transparent turtle, patch, or link
   * present in the scene, this function will return true. This is used to
   * determine whether it is necessary to sort the objects by their distance
   * to the observer before rendering, which is necessary for transparency
   * to work in OpenGL.
   *
   * @return True if the scene has at least one partially transparent object.
   */
  boolean sceneHasPartiallyTransparentObjects() {
    for (Agent agent : world.turtles().agents()) {
      if (agentIsPartiallyTransparent(agent)) {
        return true;
      }
    }

    for (Agent agent : world.patches().agents()) {
      if (agentIsPartiallyTransparent(agent)) {
        return true;
      }
    }

    for (Agent agent : world.links().agents()) {
      if (agentIsPartiallyTransparent(agent)) {
        return true;
      }
    }

    //
    // Turtle and link stamps
    //
    if (world instanceof World3D) {
      for (org.nlogo.api.Turtle stamp : ((Drawing3D) world.getDrawing()).turtleStamps()) {
        if (agentIsPartiallyTransparent(stamp)) {
          return true;
        }
      }

      for (org.nlogo.api.Link stamp : ((Drawing3D) world.getDrawing()).linkStamps()) {
        if (agentIsPartiallyTransparent(stamp)) {
          return true;
        }
      }
    }

    // In the 3D view in 2D, stamps are rasterized on the drawing layer,
    // so they're not agents and we don't need to check for them here.

    return false;
  }

  boolean agentIsPartiallyTransparent(Agent agent) {
    return getAlpha(agent) < 255;
  }

  static double getAlpha(Agent agent) {
    Object color;
    if (agent instanceof Turtle) {
      color = ((Turtle) agent).color();
    } else if (agent instanceof Patch) {
      color = ((Patch) agent).pcolor();
    } else if (agent instanceof Link) {
      color = ((Link) agent).color();
    } else {
      return 255;
    }
    // special case black, non-RGB 3D patches to be invisible.  it's a
    // kinda janky to have a special case like that, but until
    // we have an alpha variable, it seems like the least bad
    // design. - ST 4/20/11
    if (agent instanceof Patch3D && color.equals(org.nlogo.api.Color.BOXED_BLACK)) {
      return 0;
    }
    return org.nlogo.api.Color.getColor(color).getAlpha();
  }

  /**
   * Returns true if this agent should be rendered (i.e., it is not hidden, nor fully transparent,
   * and the observer is not riding this agent). Note: if this agent has a label, the agent will
   * be regarded as "visible".
   */
  boolean agentIsVisible(Agent agent) {
    if (agent instanceof Turtle) {
      Turtle turtle = (Turtle) agent;

      boolean riding_agent = (world.observer().perspective() == Perspective.RIDE)
          && (world.observer().targetAgent() == turtle);

      return !riding_agent && !turtle.hidden()
          && (getAlpha(turtle) > 0.0 || turtle.hasLabel());
    } else if (agent instanceof Link) {
      Link link = (Link) agent;
      return !link.hidden() && (getAlpha(link) > 0.0 || link.hasLabel());
    } else if (agent instanceof Patch3D) {
      // Patch3D supports the alpha variable, so check Patch3D
      // before checking the regular Patch.

      Patch3D patch = (Patch3D) agent;
      return getAlpha(patch) > 0.0 || patch.hasLabel();
    } else if (agent instanceof Patch) {
      // We will assume all patches are visible. However, perhaps
      // we should only return true for non-black patches.

      return true;
    } else {
      throw new IllegalStateException("Agent must be an instance of Turtle, Patch, or Link.");
    }
  }

  void render(GL gl) {
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

      float[] lightPosition = {-1.0f, -0.3f, 0.4f, 0.0f};
      gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, FloatBuffer.wrap(lightPosition));

      float[] lightPosition2 = {1.0f, 0.6f, -0.5f, 0.0f};
      gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, FloatBuffer.wrap(lightPosition2));

      renderWorld(gl, world);

      setClippingPlanes(gl);

      // Calculate the line scale to use for rendering outlined turtles and links,
      // as well as link stamps.
      double lineScale = calculateLineScale();

      boolean sortingNeeded = sceneHasPartiallyTransparentObjects();

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
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        opaqueAgents.clear();
        transparentAgents.clear();

        for (Agent agent : world.turtles().agents()) {
          if (agentIsVisible(agent)) {
            if (agentIsPartiallyTransparent(agent)) {
              transparentAgents.add(agent);
            } else {
              opaqueAgents.add(agent);
            }
          }
        }

        for (Agent agent : world.patches().agents()) {
          if (agentIsVisible(agent)) {
            if (agentIsPartiallyTransparent(agent)) {
              transparentAgents.add(agent);
            } else {
              opaqueAgents.add(agent);
            }
          }
        }

        for (Agent agent : world.links().agents()) {
          if (agentIsVisible(agent)) {
            if (agentIsPartiallyTransparent(agent)) {
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
              if (agentIsPartiallyTransparent(stamp)) {
                transparentAgents.add(stamp);
              } else {
                opaqueAgents.add(stamp);
              }
            }
          }

          // Turtle stamps
          for (org.nlogo.api.Turtle stamp : ((Drawing3D) world.getDrawing()).turtleStamps()) {
            if (agentIsVisible(stamp)) {
              if (agentIsPartiallyTransparent(stamp)) {
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
          // from the the org.nlogo.api package to the render package, or else we
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

        gl.glDisable(GL.GL_BLEND);
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

      if (mouseOn || pickRequest) {
        storeMatricies(gl);

        if (pickRequest) {
          performPick();
        }

        if ((perspective != Perspective.OBSERVE)
            && mouseInside && (mousePt != null)) {
          updateMouseCors();
        }
      }
    }
  }

  void renderAgent(GL gl, Agent agent, Double lineScale) {
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

  void renderWorld(GL gl, World world) {
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

    if (p == Perspective.FOLLOW || p == Perspective.RIDE) {
      distance = world.observer().followDistance();
    } else {
      distance = world.observer().dist();
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
  public void queuePick(java.awt.Point mousePt, PickListener pickListener) {
    pickRequest = true;
    this.mousePt = mousePt;
    this.pickListener = pickListener;
  }

  // pick/select objects for context menu
  void performPick() {
    List<Agent> agents = new ArrayList<Agent>(5);
    double[][] ray = generatePickRay(mousePt.getX(), (height - mousePt.getY()));
    pickPatches(agents, ray);
    pickTurtles(agents, ray);
    pickLinks(agents, ray);
    pickListener.pick(mousePt, agents);
    pickRequest = false;
  }

  // saves current transformation matricies and viewport
  private void storeMatricies(GL gl) {
    gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelMatrix);
    gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projMatrix);
    gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort);
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

      mouseXCor = xi;
      mouseYCor = yi;

      if (agents != null) {
        try {
          Patch patch = world.getPatchAt(xi, yi);
          agents.add(patch);
        } catch (AgentException e) {
          org.nlogo.util.Exceptions.ignore(e);
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

        double dist = distanceFromRayToSegment(ray, end1, end2);

        if (dist <= PICK_THRESHOLD) {
          agents.add(link);
        }
      }
    }
  }

  // adapted from code at http://softsurfer.com/Archive/algorithm_0106/algorithm_0106.htm
  // Copyright 2001, softSurfer (www.softsurfer.com)
  // This code may be freely used and modified for any purpose
  // providing that this copyright notice is included with it.
  // SoftSurfer makes no warranty for this code, and cannot be held
  // liable for any real or imagined damage resulting from its use.
  // Users of this code must verify correctness for their application.
  private static double distanceFromRayToSegment(double[][] ray, double[] end1, double[] end2) {
    Vect u = new Vect((end2[0] - end1[0]),
        (end2[1] - end1[1]),
        (end2[2] - end1[2]));
    Vect v = new Vect((ray[1][0] - ray[0][0]),
        (ray[1][1] - ray[0][1]),
        (ray[1][2] - ray[0][2]));
    Vect w = new Vect((end1[0] - ray[0][0]),
        (end1[1] - ray[0][1]),
        (end1[2] - ray[0][2]));

    double a = u.dot(u); // always >= 0
    double b = u.dot(v);
    double c = v.dot(v);  // always >= 0
    double d = u.dot(w);
    double e = v.dot(w);
    double D = a * c - b * b;       // always >= 0
    double sc, sN, sD = D;        // sc = sN / sD, default sD = D >= 0
    double tc, tN, tD = D;        // tc = tN / tD, default tD = D >= 0

    // compute the line parameters of the two closest points
    if (D < World.INFINITESIMAL) { // the lines are almost parallel
      sN = 0.0;        // force using point P0 on segment S1
      sD = 1.0;        // to prevent possible division by 0.0 later
      tN = e;
      tD = c;
    } else {  // get the closest points on the infinite lines
      sN = (b * e - c * d);
      tN = (a * e - b * d);
      if (sN < 0.0) { // sc < 0 => the s=0 edge is visible
        sN = 0.0;
        tN = e;
        tD = c;
      } else if (sN > sD) {  // sc > 1 => the s=1 edge is visible
        sN = sD;
        tN = e + b;
        tD = c;
      }
    }

    // finally do the division to get sc and tc
    sc = Math.abs(sN) < World.INFINITESIMAL ? 0.0 : sN / sD;
    tc = Math.abs(tN) < World.INFINITESIMAL ? 0.0 : tN / tD;

    // get the difference of the two closest points
    Vect dP = new Vect((u.x() * sc) - (v.x() * tc) + w.x(),
        (u.y() * sc) - (v.y() * tc) + w.y(),
        (u.z() * sc) - (v.z() * tc) + w.z()); // = S1(sc) - S2(tc)

    return dP.magnitude();   // return the closest distance
  }

  double[] getTurtleCoords(Turtle turtle, double height) {
    return new double[]{world.wrappedObserverX(turtle.xcor()) * Renderer.WORLD_SCALE,
        world.wrappedObserverY(turtle.ycor()) * Renderer.WORLD_SCALE, 0};
  }


  /// Mouse interaction

  public void setMouseMode(boolean mode) {
    mouseOn = mode;
  }

  public double mouseXCor() {
    return mouseXCor;
  }

  public double mouseYCor() {
    return mouseYCor;
  }

  public boolean mouseDown() {
    return mouseDown;
  }

  public void mouseDown(boolean mouseDown) {
    this.mouseDown = mouseDown;
  }

  public boolean mouseInside() {
    return mouseInside;
  }

  public void resetMouseCors() {
    mouseXCor = 0;
    mouseYCor = 0;
  }

  public void setMouseCors(java.awt.Point mousePt) {
    double[][] ray = generatePickRay(mousePt.getX(), (height - mousePt.getY()));
    pickPatches(null, ray);
    this.mousePt = mousePt;
  }

  public void updateMouseCors() {
    double[][] ray = generatePickRay(mousePt.getX(), (height - mousePt.getY()));
    pickPatches(null, ray);
  }

  public void mouseInside(double mx, double my) {
    double[][] ray = generatePickRay(mx, height - my);
    double scale = (1 / WORLD_SCALE);
    double t = ((ray[0][2] + (Renderer.WORLD_SCALE / 2)) /
        (ray[0][2] - ray[1][2]));
    double xi = scale * (ray[0][0] + (ray[1][0] - ray[0][0]) * t);
    double yi = scale * (ray[0][1] + (ray[1][1] - ray[0][1]) * t);

    if ((xi < world.maxPxcor() + 0.5) && (xi >= world.minPxcor() - 0.5) &&
        (yi < world.maxPycor() + 0.5) && (yi >= world.minPycor() - 0.5)) {
      mouseInside = true;
    } else {
      mouseInside = false;
    }
  }

  /// Crosshairs

  public void showCrossHairs(boolean visible) {
    worldRenderer.showCrossHairs_$eq(visible);
  }

  /// Shapes

  public void addCustomShapes(String filename)
      throws java.io.IOException,
      org.nlogo.shape.InvalidShapeDescriptionException {
    shapeManager.addShape(filename);
  }

  public void invalidateTurtleShape(String shape) {
    shapeManager.invalidateTurtleShape(shape);
  }

  public void invalidateLinkShape(String shape) {
    shapeManager.invalidateLinkShape(shape);
  }

  public void translateWorld(GL gl, World world) {
    gl.glTranslated
        ((world.maxPxcor() + world.minPxcor()) / 2.0 * Renderer.WORLD_SCALE,
            (world.maxPycor() + world.minPycor()) / 2.0 * Renderer.WORLD_SCALE,
            0.0);
  }
}
