package org.nlogo.gl.view;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.window.JOGLLoadingException;
import org.nlogo.window.JOGLVersionMismatchException;
import org.nlogo.api.I18N;

public strictfp class ViewManager
    implements org.nlogo.window.GLViewManagerInterface,
    org.nlogo.window.Event.LinkChild,
    org.nlogo.window.Event.LinkParent,
    org.nlogo.window.Events.PeriodicUpdateEvent.Handler,
    org.nlogo.gl.render.GLViewSettings {

  org.nlogo.agent.World world;
  View currentView;
  ObserverView observerView;
  FullscreenView fullscreenView = null;
  View turtleView;
  final org.nlogo.window.GUIWorkspace workspace;
  private boolean fullscreen;
  public final javax.swing.JFrame appWindow;
  private final java.awt.event.KeyListener keyListener;

  private boolean paintingImmediately = false;
  private boolean framesSkipped = false;

  public ViewManager(org.nlogo.window.GUIWorkspace workspace,
                     javax.swing.JFrame appWindow,
                     java.awt.event.KeyListener keyListener) {
    this.workspace = workspace;
    world = workspace.world();
    this.appWindow = appWindow;
    this.keyListener = keyListener;
  }

  //event link component
  public Object getLinkParent() {
    return appWindow;
  }

  public void open()
      throws JOGLLoadingException {
    if (observerView != null) {
      observerView.toFront();
      observerView.updatePerspectiveLabel();
    } else {
      try {
        init();
      } catch (JOGLVersionMismatchException vex) {
        org.nlogo.swing.Utils.alert(vex.getMessage(),
            I18N.gui().get("common.buttons.continue"));
      } catch (JOGLLoadingException ex) {
        if (observerView != null) {
          observerView.dispose();
          observerView = null;
        }
        throw ex;
      }
    }
  }

  public void init()
      throws JOGLLoadingException {
    org.nlogo.gl.render.JOGLException jvmex = null;
    if (!org.nlogo.gl.render.JOGLLoader.isLoaded()) {
      try {
        ClassLoader classLoader = getClass().getClassLoader();
        org.nlogo.gl.render.JOGLLoader.load(classLoader);
      } catch (org.nlogo.gl.render.JOGLException ex) {
        if (ex.throwImmediately()) {
          throw new JOGLLoadingException(ex.getMessage(), ex.t());
        } else {
          // We should try to initialize even if we have a version mismatch,
          // so we hold off on throwing the exception until the end of the
          // method. - AZS 6/27/05
          jvmex = ex;
        }
      }
    }

    // if we have a frame already, dispose of it
    if (observerView != null) {
      observerView.dispose();
    }

    try {
      observerView = new ObserverView(this, null);
      observerView.canvas().addKeyListener(keyListener);
      currentView = observerView;
      org.nlogo.awt.Utils.moveNextTo
          (observerView, appWindow);
      currentView.updatePerspectiveLabel();
      observerView.setVisible(true);
    } catch (java.lang.UnsatisfiedLinkError e) {
      throw new JOGLLoadingException
          ("NetLogo could not load the JOGL native libraries on your computer.\n\n" +
              "Write bugs@ccl.northwestern.edu for assistance.", e);
    }

    if (jvmex != null) {
      throw new JOGLVersionMismatchException(jvmex.getMessage());
    }
  }

  public void setFullscreen(boolean fullscreen) {
    if (fullscreen == isFullscreen()) {
      return;
    }

    java.awt.GraphicsDevice gd
        = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice();

    // this is necessary in order to force PatchRenderer to make a new texture,
    // since the old one won't survive the transition to fullscreen - ST 2/9/05
    world.markPatchColorsDirty();

    if (fullscreen) {
      if (!gd.isFullScreenSupported()) {
        throw new UnsupportedOperationException
            ("This graphics environment does not support full screen mode");
      }
      currentView.setVisible(true);
      appWindow.setVisible(false);
      fullscreenView = new FullscreenView(this, currentView.renderer());
      fullscreenView.canvas().addKeyListener(keyListener);
      fullscreenView.init();
      observerView.setVisible(false);
      currentView = fullscreenView;
      this.fullscreen = true;
    } else {
      appWindow.setVisible(true);
      gd.setFullScreenWindow(null);
      observerView.setVisible(true);
      observerView.updateRenderer();
      this.fullscreen = false;
      currentView = observerView;
      fullscreenView.dispose();
    }
  }

  public boolean isFullscreen() {
    return fullscreen;
  }

  public void editFinished() {
    if (currentView != null) {
      currentView.editFinished();
    }
  }

  public boolean is3D() {
    return currentView != null;
  }

  public boolean isDead() {
    return false;
  }

  // called when user closes window, or by GUIWorkspace when new model loaded.
  public void close() {
    workspace.set2DViewEnabled(true);
    if (currentView != null) {
      currentView.dispose();
    }
    observerView = null;
    currentView = null;
  }

  private final Runnable paintRunnable =
      new Runnable() {
        public void run() {
          incrementalUpdateFromEventThread();
        }
      };

  public void incrementalUpdateFromJobThread() {
    try {
      org.nlogo.awt.Utils.invokeAndWait(paintRunnable);
    } catch (InterruptedException ex) {
      repaint();
    }
  }

  public void incrementalUpdateFromEventThread() {
    // in case we get called before init() - ST 2/18/05
    if (currentView != null) {
      workspace.updateManager().beginPainting();
      currentView.display();
      workspace.updateManager().donePainting();
      currentView.updatePerspectiveLabel();
    }

  }

  public void repaint() {
    // in case we get called before init() - ST 2/18/05
    if (currentView != null) {
      workspace.updateManager().beginPainting();
      currentView.signalViewUpdate();
      workspace.updateManager().donePainting();
      currentView.updatePerspectiveLabel();
      framesSkipped = false;
    }
  }

  private boolean antiAliasing = true;

  public void antiAliasingOn(boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
    if (currentView != null) {
      world.markPatchColorsDirty();
      observerView = new ObserverView(this, currentView.renderer(), currentView.getBounds());
      currentView.dispose();
      currentView = observerView;
      currentView.setVisible(true);
    }
  }

  public boolean antiAliasingOn() {
    return antiAliasing;
  }

  private boolean wireframeOn = true;

  public boolean wireframeOn() {
    return wireframeOn;
  }

  public void wireframeOn(boolean on) {
    wireframeOn = on;
  }

  public void framesSkipped() {
    framesSkipped = true;
  }

  public void paintingImmediately(boolean paintingImmediately) {
    this.paintingImmediately = paintingImmediately;
  }

  public boolean paintingImmediately() {
    return paintingImmediately;
  }

  public void paintImmediately(boolean force) {
    if (viewIsVisible() && (framesSkipped || force)) {
      paintingImmediately(true);
      repaint();
      paintingImmediately(false);
    }
  }

  public boolean viewIsVisible() {
    return currentView.isShowing();
  }

  public java.awt.Component getExportWindowFrame() {
    return currentView;
  }

  public java.awt.image.BufferedImage exportView() {
    return currentView.exportView();
  }

  private final List<Object> linkComponents = new ArrayList<Object>();

  public void addLinkComponent(Object c) {
    linkComponents.clear();
    linkComponents.add(c);
  }

  public Object[] getLinkChildren() {
    return linkComponents.toArray();
  }

  public void handle(org.nlogo.window.Events.PeriodicUpdateEvent e) {
    if (observerView != null) {
      observerView.controlStrip().updateTicks();
    }
  }

  public boolean displayOn() {
    return workspace.displaySwitchOn();
  }

  public void displayOn(boolean displayOn) {
    workspace.displaySwitchOn(displayOn);
  }

  public org.nlogo.api.ViewSettings graphicsSettings() {
    return workspace.view;
  }

  public double mouseXCor() {
    if (currentView != null) {
      return currentView.renderer().mouseXCor();
    }
    return 0.0f;
  }

  public double mouseYCor() {
    if (currentView != null) {
      return currentView.renderer().mouseYCor();
    }
    return 0.0f;
  }

  public void resetMouseCors() {
    if (currentView != null) {
      currentView.renderer().resetMouseCors();
    }
  }

  public boolean mouseDown() {
    if (currentView != null) {
      return currentView.renderer().mouseDown();
    }
    return false;
  }

  public boolean mouseInside() {
    return currentView.renderer().mouseInside();
  }

  public void shapeChanged(org.nlogo.api.Shape shape) {
    if (currentView != null) {
      if (shape instanceof org.nlogo.shape.VectorShape) {
        currentView.invalidateTurtleShape(shape.getName());
      } else {
        currentView.invalidateLinkShape(shape.getName());
      }
      repaint();
    }
  }

  public void addCustomShapes(String filename)
      throws java.io.IOException,
      org.nlogo.shape.InvalidShapeDescriptionException {
    currentView.renderer().addCustomShapes(filename);
  }

  public void displaySwitch(boolean on) {
    observerView.controlStrip().displaySwitch().setOn(on);
  }

  public boolean displaySwitch() {
    if (observerView != null) {
      return observerView.controlStrip().displaySwitch().isSelected();
    }
    throw new IllegalStateException();
  }

  public void applyNewFontSize(int fontSize, int zoom) {
    /// I think the 3D renderer grabs it's font size
    // directly from the main view so we don't
    // need to keep track of it here.
  }

  private boolean warned = false;

  boolean warned() {
    return warned;
  }

  void warned(boolean warned) {
    this.warned = warned;
  }
}
