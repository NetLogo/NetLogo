// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import scala.collection.Seq;
import scala.Tuple2;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.BooleanConstraint;
import org.nlogo.agent.Observer;
import org.nlogo.agent.SliderConstraint;
import org.nlogo.core.ShapeParser;
import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.CompilerException;
import org.nlogo.core.I18N;
import org.nlogo.core.UpdateMode;
import org.nlogo.core.UpdateModeJ;
import org.nlogo.api.AgentFollowingPerspective;
import org.nlogo.api.CommandRunnable;
import org.nlogo.api.LogoException;
import org.nlogo.api.ModelSectionJ;
import org.nlogo.api.ModelTypeJ;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.PreviewCommands$;
import org.nlogo.api.RendererInterface;
import org.nlogo.api.ReporterRunnable;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.shape.ShapeConverter;
import org.nlogo.log.Logger;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Workspace;

import java.util.HashMap;
import java.util.ArrayList;

public abstract strictfp class GUIWorkspace // can't be both abstract and strictfp
    extends org.nlogo.workspace.AbstractWorkspaceScala
    implements
    org.nlogo.window.Event.LinkChild,
    org.nlogo.window.Events.AboutToQuitEvent.Handler,
    org.nlogo.window.Events.AddJobEvent.Handler,
    org.nlogo.window.Events.AfterLoadEvent.Handler,
    org.nlogo.window.Events.BeforeLoadEvent.Handler,
    org.nlogo.window.Events.ExportPlotEvent.Handler,
    org.nlogo.window.Events.JobStoppingEvent.Handler,
    org.nlogo.window.Events.LoadModelEvent.Handler,
    org.nlogo.window.Events.RemoveAllJobsEvent.Handler,
    org.nlogo.window.Events.RemoveJobEvent.Handler,
    org.nlogo.window.Events.AddSliderConstraintEvent.Handler,
    org.nlogo.window.Events.RemoveConstraintEvent.Handler,
    org.nlogo.window.Events.AddBooleanConstraintEvent.Handler,
    org.nlogo.window.Events.AddChooserConstraintEvent.Handler,
    org.nlogo.window.Events.AddInputBoxConstraintEvent.Handler,
    org.nlogo.window.Events.CompiledEvent.Handler,
    org.nlogo.api.TrailDrawerInterface,
    org.nlogo.api.DrawingInterface,
    org.nlogo.api.ModelSections.ModelSaveable {

  public enum KioskLevel {NONE, MODERATE}

  public final KioskLevel kioskLevel;

  private final java.awt.Frame frame;
  private final java.awt.Component linkParent;
  public final ViewWidget viewWidget;
  public final View view;
  private WidgetContainer widgetContainer = null;
  public GLViewManagerInterface glView = null;
  public ViewManager viewManager = new ViewManager();
  private final ExternalFileManager externalFileManager;
  public final NetLogoListenerManager listenerManager;

  // for grid snap
  private boolean snapOn = false;

  private PeriodicUpdater periodicUpdater;
  private javax.swing.Timer repaintTimer;
  private Lifeguard lifeguard;

  public GUIWorkspace(final org.nlogo.agent.World world,
                      KioskLevel kioskLevel, java.awt.Frame frame,
                      java.awt.Component linkParent,
                      org.nlogo.workspace.HubNetManagerFactory hubNetManagerFactory,
                      ExternalFileManager externalFileManager,
                      NetLogoListenerManager listenerManager) {
    super(world, hubNetManagerFactory);
    this.kioskLevel = kioskLevel;
    this.frame = frame;
    this.linkParent = linkParent;
    this.externalFileManager = externalFileManager;
    this.listenerManager = listenerManager;
    hubNetControlCenterAction.setEnabled(false);

    viewWidget = new ViewWidget(this);
    view = viewWidget.view();
    viewManager.setPrimary(view);

    periodicUpdater = new PeriodicUpdater(jobManager);
    periodicUpdater.start();
    world.trailDrawer(this);

    // ensure that any skipped frames get painted eventually
    javax.swing.Action repaintAction =
        new javax.swing.AbstractAction() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            if (world.displayOn() && displaySwitchOn() && !jobManager.anyPrimaryJobs()) {
              viewManager.paintImmediately(world.observer().updatePosition());
            }
          }
        };
    // 10 checks a second seems like plenty
    repaintTimer = new javax.swing.Timer(100, repaintAction);
    repaintTimer.start();

    lifeguard = new Lifeguard();
    lifeguard.start();
  }

  // Lifeguard ensures the engine comes up for air every so often.
  // we use a separate thread and not a Swing timer because Swing
  // timers run on the event thread, but we need to make sure the
  // engine comes up for air even when the event thread is blocked,
  // since often the reason the event thread is blocked is exactly
  // because it needs the engine to come up for air! - ST 9/4/07

  // forcing the engine to come up for air serves several purposes;
  // it makes sure Tools -> Halt as opportunities to take effect,
  // and it also gives us opportunities for view updates when we're
  // using continuous updates - ST 3/1/11

  private class Lifeguard extends Thread {
    Lifeguard() {
      super("Lifeguard");
    }

    @Override
    public void run() {
      try {
        while (true) {
          if (jobManager.anyPrimaryJobs()) {
            world().comeUpForAir = true;
          }
          // 100 times a second seems like plenty
          Thread.sleep(10);
        }
      } catch (InterruptedException ex) {
        // ignore because we may be interrupted during
        // applet shutdown, e.g. in Camino - ST 11/29/07
        org.nlogo.api.Exceptions.ignore(ex);
      }
    }
  }

  public void init(GLViewManagerInterface glView) {
    this.glView = glView;
  }

  private double _frameRate = 30.0;

  public double frameRate() {
    return _frameRate;
  }

  public void frameRate(double frameRate) {
    _frameRate = frameRate;
    updateManager().recompute();
  }

  public abstract UpdateManagerInterface updateManager();

  public abstract RendererInterface newRenderer();

  public void stamp(org.nlogo.api.Agent agent, boolean erase) {
    view.renderer.prepareToPaint(view, view.renderer.trailDrawer().getWidth(), view.renderer.trailDrawer().getHeight());
    view.renderer.trailDrawer().stamp(agent, erase);
    if (hubNetManager().isDefined()) {
      hubNetManager().get().sendStamp(agent, erase);
    }
  }

  @Override
  public void importWorld(String filename) throws java.io.IOException {
    super.importWorld(filename);
    new org.nlogo.window.Events.TickStateChangeEvent(true).raiseLater(this);
  }

  @Override
  public void importWorld(java.io.Reader reader) throws java.io.IOException {
    super.importWorld(reader);
    new org.nlogo.window.Events.TickStateChangeEvent(true).raiseLater(this);
  }

  @Override
  public void importDrawing(java.io.InputStream is)
      throws java.io.IOException {
    view.renderer.trailDrawer().importDrawing(is);
  }

  @Override
  public void importDrawing(org.nlogo.core.File file)
      throws java.io.IOException {
    view.renderer.trailDrawer().importDrawing(file);
  }

  public void exportDrawing(String filename, String format)
      throws java.io.IOException {
    java.io.FileOutputStream stream =
        new java.io.FileOutputStream(new java.io.File(filename));
    javax.imageio.ImageIO.write
        (view.renderer.trailDrawer().getAndCreateDrawing(true), format, stream);
    stream.close();
  }

  public java.awt.image.BufferedImage getAndCreateDrawing() {
    return getAndCreateDrawing(true);
  }

  public java.awt.image.BufferedImage getAndCreateDrawing(boolean dirty) {
    return view.renderer.trailDrawer().getAndCreateDrawing(dirty);
  }

  @Override
  public void clearDrawing() {
    world().clearDrawing();
    view.renderer.trailDrawer().clearDrawing();
    if (hubNetManager().isDefined()) {
      hubNetManager().get().sendClear();
    }
  }

  @Override
  public void resetTicks(org.nlogo.nvm.Context context) {
    super.resetTicks(context);
    new Events.TickStateChangeEvent(true).raiseLater(this);
  }

  @Override
  public void clearTicks() {
    super.clearTicks();
    new Events.TickStateChangeEvent(false).raiseLater(this);
  }

  @Override
  public void clearAll() {
    super.clearAll();
    new Events.TickStateChangeEvent(false).raiseLater(this);
  }

  public boolean sendPixels() {
    return view.renderer.trailDrawer().sendPixels();
  }

  public void sendPixels(boolean dirty) {
    view.renderer.trailDrawer().sendPixels(dirty);
  }

  @Override
  public void dispose() throws InterruptedException {
    periodicUpdater.stop();
    repaintTimer.stop();
    lifeguard.interrupt();
    lifeguard.join();
    super.dispose();
  }

  public WidgetContainer getWidgetContainer() {
    return widgetContainer;
  }

  public void setWidgetContainer(WidgetContainer widgetContainer) {
    this.widgetContainer = widgetContainer;
  }

  @Override
  public boolean isHeadless() {
    return false;
  }

  public void waitFor(Runnable runnable) {
    ThreadUtils.waitFor(this, runnable);
  }

  public void waitFor(CommandRunnable runnable)
      throws LogoException {
    ThreadUtils.waitFor(this, runnable);
  }

  public <T> T waitForResult(ReporterRunnable<T> runnable)
      throws LogoException {
    return ThreadUtils.waitForResult(this, runnable);
  }

  public void waitForQueuedEvents()
      throws LogoException {
    ThreadUtils.waitForQueuedEvents(this);
  }

  /// Event.LinkChild stuff

  public Object getLinkParent() {
    return linkParent;
  }

  /**
   * Displays a warning to the user, allowing her to continue or cancel.
   * This provides the nice graphical warning dialog for when we're GUI.
   * Returns true if the user OKs it.
   */
  @Override
  public boolean warningMessage(String message) {
    String[] options = {I18N.guiJ().get("common.buttons.continue"), I18N.guiJ().get("common.buttons.cancel")};
    return 0 == org.nlogo.swing.OptionDialog.show(
        getFrame(), I18N.guiJ().get("common.messages.warning"),
        "Warning: " + message, options);
  }

  public void resizeView() {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
    viewWidget.settings().resizeWithProgress(true);
  }

  public void patchSize(double patchSize) {
    viewWidget.settings().patchSize(patchSize);
  }

  public double patchSize() {
    return world().patchSize();
  }

  public void setDimensions(final org.nlogo.core.WorldDimensions d) {
    Runnable runner =
        new Runnable() {
          public void run() {
            viewWidget.settings().setDimensions(d);
          }
        };
    // this may be called from _resizeworld in which case we're
    // already on the event thread - ST 7/21/09
    if (java.awt.EventQueue.isDispatchThread()) {
      runner.run();
    } else {
      try {
        org.nlogo.awt.EventQueue.invokeAndWait(runner);
      } catch (InterruptedException ex) {
        org.nlogo.api.Exceptions.handle(ex);
      }
    }
  }

  public void setDimensions(final org.nlogo.core.WorldDimensions d, final double patchSize) {
    Runnable runner =
        new Runnable() {
          public void run() {
            viewWidget.settings().setDimensions(d, patchSize);
          }
        };
    // this may be called from _setpatchsize in which case we're
    // already on the event thread - ST 7/21/09
    if (java.awt.EventQueue.isDispatchThread()) {
      runner.run();
    } else {
      try {
        org.nlogo.awt.EventQueue.invokeAndWait(runner);
      } catch (InterruptedException ex) {
        org.nlogo.api.Exceptions.handle(ex);
      }
    }
  }

  public void patchesCreatedNotify() {
    new org.nlogo.window.Events.PatchesCreatedEvent().raise(this);
  }

  public java.awt.Frame getFrame() {
    return frame;
  }

  public boolean compilerTestingMode() {
    return false;
  }

  @Override
  public org.nlogo.api.WorldPropertiesInterface getPropertiesInterface() {
    return viewWidget.settings();
  }

  public void changeTopology(boolean wrapX, boolean wrapY) {
    world().changeTopology(wrapX, wrapY);
    viewWidget.view().renderer.changeTopology(wrapX, wrapY);
  }

  /// very kludgy stuff for communicating with stuff in app
  /// package without having any compile-time dependencies on it

  // called from an "other" thread (neither event thread nor job thread)
  @Override
  public void open(final String path) {
    try {
      org.nlogo.awt.EventQueue.invokeAndWait
          (new Runnable() {
            public void run() {
              new org.nlogo.window.Events.OpenModelEvent(path)
                  .raise(GUIWorkspace.this);
            }
          });
    } catch (InterruptedException ex) {
      throw new IllegalStateException(ex);
    }
  }

  // Right now I only need this for HeadlessWorkspace, for parallel BehaviorSpace - ST 3/12/09
  @Override
  public void openString(String modelContents) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void openModel(org.nlogo.core.Model model) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RendererInterface renderer() {
    return view.renderer;
  }

  // called from the job thread
  public void reload() {
    new org.nlogo.window.Events.AppEvent
        (AppEventType.RELOAD, new Object[]{})
        .raiseLater(this);
  }

  // called from the job thread
  @Override
  public void magicOpen(String name) {
    new org.nlogo.window.Events.AppEvent
        (AppEventType.MAGIC_OPEN, new Object[]{name})
        .raiseLater(this);
  }

  // called from the job thread
  @Override
  public void changeLanguage() {
    new org.nlogo.window.Events.AppEvent(AppEventType.CHANGE_LANGUAGE, new Object[]{}).raiseLater(this);
  }


  // called from the job thread
  public void startLogging(String properties) {
    try {
      new org.nlogo.window.Events.AppEvent
          (AppEventType.START_LOGGING,
              new Object[]{fileManager().attachPrefix(properties)})
          .raiseLater(this);
    } catch (java.net.MalformedURLException ex) {
      throw new IllegalStateException(ex);
    }
  }

  // called from the job thread
  public void zipLogFiles(String filename) {
    try {
      new org.nlogo.window.Events.AppEvent
          (AppEventType.ZIP_LOG_FILES,
              new Object[]{fileManager().attachPrefix(filename)})
          .raiseLater(this);
    } catch (java.net.MalformedURLException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public void deleteLogFiles() {
    new org.nlogo.window.Events.AppEvent
        (AppEventType.DELETE_LOG_FILES, new Object[]{})
        .raiseLater(this);
  }

  /// painting

  public boolean displaySwitchOn() {
    return viewManager.getPrimary().displaySwitch();
  }

  public void displaySwitchOn(boolean on) {
    viewManager.getPrimary().displaySwitch(on);
  }

  public void set2DViewEnabled(boolean enabled) {
    if (enabled) {
      displaySwitchOn(glView.displayOn());

      viewManager.setPrimary(view);
      viewManager.remove(glView);

      view.dirty();
      if (glView.displayOn()) {
        view.thaw();
      }
      if (! (world().observer().perspective() instanceof AgentFollowingPerspective)) {
        world().observer().home();
      }
      viewWidget.setVisible(true);
      try {
        viewWidget.displaySwitch().setOn(glView.displaySwitch());
      } catch (IllegalStateException e) {
        org.nlogo.api.Exceptions.ignore(e);
      }
    } else {
      viewManager.setPrimary(glView);

      if (!dualView) {
        viewManager.remove(view);
        view.freeze();
      }
      glView.displaySwitch(viewWidget.displaySwitch().isSelected());
      viewWidget.setVisible(dualView);
    }
    view.renderPerspective = enabled;
    viewWidget.settings().refreshViewProperties(!enabled);
    new org.nlogo.window.Events.Enable2DEvent(enabled).raise(this);
  }

  private boolean dualView;

  public boolean dualView() {
    return dualView;
  }

  public void dualView(boolean on) {
    if (on != dualView) {
      dualView = on;
      if (dualView) {
        view.thaw();
        viewManager.setSecondary(view);
      } else {
        view.freeze();
        viewManager.remove(view);
      }
      viewWidget.setVisible(on);
    }
  }


  // when we've got two views going the mouse reporters should
  // be smart about which view we might be in and return something that makes
  // sense ev 12/20/07
  public boolean mouseDown()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse clicks - ST 5/3/04
    waitForQueuedEvents();
    return viewManager.mouseDown();
  }

  public boolean mouseInside()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents();
    return viewManager.mouseInside();
  }

  public double mouseXCor()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents();
    return viewManager.mouseXCor();
  }

  public double mouseYCor()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents();
    return viewManager.mouseYCor();
  }

  // shouldn't have to fully qualify UpdateMode here, but we were having
  // intermittent compile failures on this line since upgrading to
  // Scala 2.8.0.RC1 - ST 4/16/10
  @Override
  public void updateMode(UpdateMode updateMode) {
    super.updateMode(updateMode);
    updateManager().recompute();
  }

  // Translate between the physical position of the speed slider and
  // the abstract speed value.  The slider has an area in the center
  // where the speed is 0 regardless of the precise position, and the
  // scale is different. - ST 3/3/11
  public double speedSliderPosition() {
    double s = updateManager().speed() * 2;
    if (s > 0) {
      s += 10;
    } else if (s < 0) {
      s -= 10;
    }
    return s;
  }

  public void speedSliderPosition(double speed) {
    updateManager().speed_$eq(speed);
  }

  // this is called *only* from job thread - ST 8/20/03, 1/15/04
  public void updateDisplay(boolean haveWorldLockAlready) {
    view.dirty();
    if (!world().displayOn()) {
      return;
    }
    if (!updateManager().shouldUpdateNow()) {
      viewManager.framesSkipped();
      return;
    }
    if (!displaySwitchOn()) {
      return;
    }
    if (haveWorldLockAlready) {
      try {
        waitFor
            (new org.nlogo.api.CommandRunnable() {
              public void run() {
                viewManager.incrementalUpdateFromEventThread();
              }
            });
        // don't block the event thread during a smoothing pause
        // or the UI will go sluggish (issue #1263) - ST 9/21/11
        while(!updateManager().isDoneSmoothing()) {
          ThreadUtils.waitForQueuedEvents(this);
        }
      } catch (org.nlogo.nvm.HaltException ex) {
        org.nlogo.api.Exceptions.ignore(ex);
      } catch (LogoException ex) {
        throw new IllegalStateException(ex);
      }
    } else {
      viewManager.incrementalUpdateFromJobThread();
    }
    updateManager().pause();
  }

  /// Job manager stuff

  private final Runnable updateRunner =
      new Runnable() {
        public void run() {
          new org.nlogo.window.Events.PeriodicUpdateEvent()
              .raise(GUIWorkspace.this);
        }
      };

  private boolean periodicUpdatesEnabled = false;

  public void setPeriodicUpdatesEnabled(boolean periodicUpdatesEnabled) {
    this.periodicUpdatesEnabled = periodicUpdatesEnabled;
  }

  // this is called on the job thread - ST 9/30/03
  public void periodicUpdate() {
    if (periodicUpdatesEnabled) {
      ThreadUtils.waitFor(this, updateRunner);
    }
  }

  // this is called on the job thread when the engine comes up for air - ST 1/10/07
  @Override
  public void breathe() {
    jobManager.maybeRunSecondaryJobs();
    if (updateMode().equals(UpdateModeJ.CONTINUOUS())) {
      updateManager().pseudoTick();
      updateDisplay(true);
    }
    world().comeUpForAir = updateManager().shouldComeUpForAirAgain();
    notifyListeners();
  }

  // called only from job thread, by such primitives as
  // _exportinterface and _usermessage, which need to make sure the
  // whole UI is up-to-date before proceeding - ST 8/30/07, 3/3/11
  public void updateUI() {
    // this makes the tick counter et al update
    ThreadUtils.waitFor(this, updateRunner);
    // resetting first ensures that if we are allowed to update the view, we will
    updateManager().reset();
    requestDisplayUpdate(true);
  }

  // on the job thread,
  // - updateUI() calls requestDisplayUpdate(true)
  // - _display, _tick, _reset-ticks call requestDisplayUpdate(true)
  // - _tickadvance calls requestDisplayUpdate(false)
  // - ST 1/4/07, 3/3/11
  @Override
  public void requestDisplayUpdate(boolean force) {
    if (force) {
      updateManager().pseudoTick();
    }
    updateDisplay(true); // haveWorldLockAlready = true
    notifyListeners();
  }

  private double lastTicksListenersHeard = -1.0;

  private void notifyListeners() {
    double ticks = world().tickCounter.ticks();
    if (ticks != lastTicksListenersHeard) {
      lastTicksListenersHeard = ticks;
      listenerManager.tickCounterChanged(ticks);
    }
    listenerManager.possibleViewUpdate();
  }

  @Override
  public void halt() {
    jobManager.interrupt();
    org.nlogo.swing.ModalProgressTask.apply(
      getFrame(), "Halting...",
      new Runnable() {
        public void run() {
          GUIWorkspace.super.halt();
          view.dirty();
          view.repaint();
        }});
  }

  // for notification of a changed shape
  public void shapeChanged(org.nlogo.core.Shape shape) {
    viewManager.shapeChanged(shape);
  }

  public void handle(org.nlogo.window.Events.AfterLoadEvent e) {
    setPeriodicUpdatesEnabled(true);
    world().observer().resetPerspective();
    updateManager().reset();
    updateManager().speed_$eq(0);
    // even when we're in 3D close the window first
    // then reopen it as the shapes won't get loaded
    // properly otherwise ev 2/24/06
    if (glView != null) {
      glView.close();
    }
    if (world().program().dialect().is3D()) {
      open3DView();
    }

    try {
      evaluateCommands(new SimpleJobOwner("startup", world().mainRNG(), AgentKindJ.Observer()),
          "without-interruption [ startup ]", false);
    } catch (CompilerException error) {
      org.nlogo.api.Exceptions.ignore(error);
    }
  }

  private void open3DView() {
    try {
      glView.open();
      set2DViewEnabled(false);
    } catch (JOGLLoadingException jlex) {
      String message = jlex.getMessage();
      org.nlogo.swing.Utils.alert
          ("3D View", message, "" + jlex.getCause(), I18N.guiJ().get("common.buttons.continue"));
      switchTo3DViewAction.setEnabled(false);
    }
  }

  public void addCustomShapes(String filename)
      throws java.io.IOException,
      org.nlogo.shape.InvalidShapeDescriptionException {
    try {
      glView.addCustomShapes(fileManager().attachPrefix(filename));
    } catch (java.net.MalformedURLException ex) {
      throw new IllegalStateException(ex);
    }
  }

  // DrawingInterface for 3D renderer
  public int[] colors() {
    return view.renderer.trailDrawer().colors();
  }

  public boolean isDirty() {
    return view.renderer.trailDrawer().isDirty();
  }

  public boolean isBlank() {
    return view.renderer.trailDrawer().isBlank();
  }

  public void markClean() {
    view.renderer.trailDrawer().markClean();
  }

  public void markDirty() {
    view.renderer.trailDrawer().markDirty();
  }

  public int getWidth() {
    return view.renderer.trailDrawer().getWidth();
  }

  public int getHeight() {
    return view.renderer.trailDrawer().getHeight();
  }

  public void readImage(java.io.InputStream is) throws java.io.IOException {
    view.renderer.trailDrawer().readImage(is);
  }

  public void readImage(java.awt.image.BufferedImage image) throws java.io.IOException {
    view.renderer.trailDrawer().readImage(image);
  }

  public void rescaleDrawing() {
    view.renderer.trailDrawer().rescaleDrawing();
  }

  public void drawLine(double x0, double y0, double x1, double y1,
                       Object color, double size, String mode) {
    view.renderer.trailDrawer().drawLine
        (x0, y0, x1, y1, color, size, mode);
    if (hubNetManager().isDefined()) {
      hubNetManager().get().sendLine(x0, y0, x1, y1, color, size, mode);
    }
  }

  public void setColors(int[] colors) {
    view.renderer.trailDrawer().setColors(colors);
  }

  public Object getDrawing() {
    return view.renderer.trailDrawer().getDrawing();
  }

  // called on job thread, but without world lock - ST 9/12/07
  public void ownerFinished(org.nlogo.api.JobOwner owner) {
    new org.nlogo.window.Events.JobRemovedEvent(owner).raiseLater(this);
    if (owner.ownsPrimaryJobs()) {
      updateManager().reset();
      updateDisplay(false);
    }
  }

  public void handle(org.nlogo.window.Events.AddJobEvent e) {
    org.nlogo.api.JobOwner owner = e.owner;
    AgentSet agents = e.agents;
    if (owner instanceof JobWidget &&
        agents == null) {
      JobWidget widget = (JobWidget) owner;
      if (widget.useAgentClass()) {
        agents = world().agentKindToAgentSet(widget.kind());
      }
    }
    if (owner.ownsPrimaryJobs()) {
      if (e.procedure != null) {
        jobManager.addJob(owner, agents, this, e.procedure);
      } else {
        new org.nlogo.window.Events.JobRemovedEvent(owner).raiseLater(this);
      }
    } else {
      jobManager.addSecondaryJob(owner, agents, this, e.procedure);
    }
  }

  public void handle(org.nlogo.window.Events.RemoveJobEvent e) {
    org.nlogo.api.JobOwner owner = e.owner;
    if (owner.ownsPrimaryJobs()) {
      jobManager.finishJobs(owner);
    } else {
      jobManager.finishSecondaryJobs(owner);
    }
  }

  public void handle(org.nlogo.window.Events.JobStoppingEvent e) {
    jobManager.stoppingJobs(e.owner);
  }

  public void handle(org.nlogo.window.Events.RemoveAllJobsEvent e) {
    jobManager.haltSecondary();
    jobManager.haltPrimary();
  }

  public void handle(org.nlogo.window.Events.AddBooleanConstraintEvent e) {
    BooleanConstraint con =
        new BooleanConstraint(e.defaultValue);

    // now we set the constraint in the observer, so that it is enforced.
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());

    if (index != -1) {
      world().observer().variableConstraint(index, con);
    }
  }

  public void handle(org.nlogo.window.Events.AddInputBoxConstraintEvent e) {
    // now we set the constraint in the observer, so that it is enforced.
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());

    if (index != -1) {
      world().observer().variableConstraint(index, e.constraint);
    }
  }

  public void handle(org.nlogo.window.Events.AddChooserConstraintEvent e) {
    // now we set the constraint in the observer, so that it is enforced.
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());

    if (index != -1) {
      world().observer().variableConstraint(index, e.constraint);
    }
  }


  public void handle(org.nlogo.window.Events.AddSliderConstraintEvent e) {
    try {
      SliderConstraint con = SliderConstraint.makeSliderConstraint
          (world().observer(), e.minSpec, e.maxSpec, e.incSpec, e.value, e.slider.name(), this);
      e.slider.removeAllErrors();
      e.slider.setSliderConstraint(con);
      // now we set the constraint in the observer, so that it is enforced.
      int index = world().observerOwnsIndexOf(e.varname.toUpperCase());
      if (index != -1) {
        world().observer().variableConstraint(index, con);
      }
    } catch (SliderConstraint.ConstraintExceptionHolder ex) {
      for (SliderConstraint.SliderConstraintException cce :
             scala.collection.JavaConversions.asJavaIterable(ex.getErrors())) {
        e.slider.setConstraintError(cce.spec().fieldName(), cce);
      }
    }
  }

  public void handle(org.nlogo.window.Events.RemoveConstraintEvent e) {
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());
    if (index != -1) {
      world().observer().variableConstraint(index, null);
    }
  }

  public void handle(org.nlogo.window.Events.CompiledEvent e) {
    codeBits.clear();
  }

  /// agents

  public abstract void closeAgentMonitors();

  public abstract void inspectAgent(AgentKind agentClass, org.nlogo.agent.Agent agent, double radius);

  public void inspectAgent(AgentKind agentClass) {
    inspectAgent(agentClass, null, (world().worldWidth() - 1) / 2);
  }

  /// output

  public void clearOutput() {
    final org.nlogo.window.Events.OutputEvent event =
        new org.nlogo.window.Events.OutputEvent
            (true, null, false, false);

    // This method can be called when we are ALREADY in the AWT
    // event thread, so check before we block on it. -- CLB 07/18/05
    if (!java.awt.EventQueue.isDispatchThread()) {
      ThreadUtils.waitFor
          (this, new Runnable() {
            public void run() {
              event.raise(GUIWorkspace.this);
            }
          });
    } else {
      event.raise(this);
    }
  }

  @Override
  protected void sendOutput(final org.nlogo.agent.OutputObject oo,
                            final boolean toOutputArea) {
    final org.nlogo.window.Events.OutputEvent event =
        new org.nlogo.window.Events.OutputEvent
            (false, oo, false, !toOutputArea);

    // This method can be called when we are ALREADY in the AWT
    // event thread, so check before we block on it. -- CLB 07/18/05
    if (!java.awt.EventQueue.isDispatchThread()) {
      ThreadUtils.waitFor
          (this, new Runnable() {
            public void run() {
              event.raise(GUIWorkspace.this);
            }
          });
    } else {
      event.raise(this);
    }

  }

  /// importing

  @Override
  protected org.nlogo.agent.Importer.ErrorHandler importerErrorHandler() {
    return new org.nlogo.agent.Importer.ErrorHandler() {
      public boolean showError(String title, String errorDetails,
                               boolean fatalError) {
        org.nlogo.awt.EventQueue.mustBeEventDispatchThread();
        String[] options = fatalError ? new String[]{I18N.guiJ().get("common.buttons.ok")} :
            new String[]{I18N.guiJ().get("common.buttons.continue"), I18N.guiJ().get("common.buttons.cancel")};
        return org.nlogo.swing.OptionDialog.show
            (getFrame(), title, errorDetails, options) == 0;
      }
    };
  }

  /// exporting

  public java.awt.Component getExportWindowFrame() {
    return viewManager.getPrimary().getExportWindowFrame();
  }

  public java.awt.image.BufferedImage exportView() {
    return viewManager.getPrimary().exportView();
  }

  public void exportView(String filename, String format)
      throws java.io.IOException {
    exportView(viewManager.getPrimary(), filename, format);
  }

  public void exportView(LocalViewInterface display, String filename, String format)
      throws java.io.IOException {
    java.io.FileOutputStream stream =
        new java.io.FileOutputStream(new java.io.File(filename));
    javax.imageio.ImageIO.write(display.exportView(), format, stream);
    stream.close();
  }

  public void doExportView(final LocalViewInterface exportee) {
    try {
      final String exportPath =
          org.nlogo.swing.FileDialog.show
              (getExportWindowFrame(), "Export View",
                  java.awt.FileDialog.SAVE,
                  guessExportName("view.png"));
      final java.io.IOException[] exception =
          new java.io.IOException[]{null};
      org.nlogo.swing.ModalProgressTask.apply(
        getFrame(),
        "Exporting...",
        new Runnable() {
          public void run() {
            try {
              exportView(exportee, exportPath, "png");
            } catch (java.io.IOException ex) {
              exception[0] = ex;
            }}});
      if (exception[0] != null) {
        throw exception[0];
      }
    } catch (org.nlogo.awt.UserCancelException ex) {
      org.nlogo.api.Exceptions.ignore(ex);
    } catch (java.io.IOException ex) {
      javax.swing.JOptionPane.showMessageDialog
          (getExportWindowFrame(), ex.getMessage(),
              I18N.guiJ().get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE);
    }
  }

  public void exportInterface(String filename)
      throws java.io.IOException {
    // there's a form of ImageIO.write that just takes a filename, but
    // if we use that when the filename is invalid (e.g. refers to
    // a directory that doesn't exist), we get an IllegalArgumentException
    // instead of an IOException, so we make our own OutputStream
    // so we get the proper exceptions. - ST 8/19/03, 11/26/03
    java.io.FileOutputStream stream =
        new java.io.FileOutputStream(new java.io.File(filename));
    java.io.IOException[] exceptionBox = new java.io.IOException[1];
    new org.nlogo.window.Events.ExportInterfaceEvent(stream, exceptionBox)
        .raise(this);
    stream.close();
    if (exceptionBox[0] != null) {
      throw exceptionBox[0];
    }
  }


  public void exportOutput(String filename) {
    new org.nlogo.window.Events.ExportOutputEvent(filename)
        .raise(this);
  }

  @Override
  public void exportDrawingToCSV(java.io.PrintWriter writer) {
    view.renderer.trailDrawer().exportDrawingToCSV(writer);
  }

  @Override
  public void exportOutputAreaToCSV(java.io.PrintWriter writer) {
    new org.nlogo.window.Events.ExportWorldEvent(writer)
        .raise(GUIWorkspace.this);
  }

  public void exportPlot(PlotWidgetExportType whichPlots, org.nlogo.plot.Plot plot,
                         String filename) {
    new org.nlogo.window.Events.ExportPlotEvent
        (whichPlots, plot, filename)
        .raise(this);
  }


  public void handle(org.nlogo.window.Events.ExportPlotEvent e) {
    if (e.whichPlots == PlotWidgetExportType.ALL) {
      if (plotManager().getPlotNames().length == 0) {
        org.nlogo.swing.OptionDialog.show
            (getFrame(), "Export Plot", "There are no plots to export.",
                new String[]{I18N.guiJ().get("common.buttons.ok")});
        return;
      }
      try {
        super.exportAllPlots(e.filename);
      } catch (java.io.IOException ex) {
        String message = "Export of all plots to" + e.filename + " failed: " + ex.getMessage();
        String[] options = {I18N.guiJ().get("common.buttons.ok")};
        org.nlogo.swing.OptionDialog.show(getFrame(), "Export Plot Failed", message, options);
      }
    } else {
      org.nlogo.plot.Plot plot = e.plot;
      if (plot == null) {
        plot = choosePlot(getFrame());
      }
      if (plot != null) {
        try {
          super.exportPlot(plot.name(), e.filename);
        } catch (java.io.IOException ex) {
          String message = "Export of " + plot.name() + " plot to " + e.filename + " failed: " + ex.getMessage();
          String[] options = {I18N.guiJ().get("common.buttons.ok")};
          org.nlogo.swing.OptionDialog.show(getFrame(), "Export Plot Failed", message, options);
        }
      }
    }
  }

  /// exporting helpers

  org.nlogo.plot.Plot choosePlot(java.awt.Frame frame) {
    String[] plotNames = plotManager().getPlotNames();
    if (plotNames.length == 0) {
      String message = "There are no plots to export.";
      String[] options = {I18N.guiJ().get("common.buttons.ok")};
      org.nlogo.swing.OptionDialog.show(frame, "Export Plot", message, options);
      return null;
    }
    String message = "Which plot would you like to export?";
    int plotnum = org.nlogo.swing.OptionDialog.showAsList
        (frame, "Export Plot",
            message, plotNames);
    if (plotnum < 0) {
      return null;
    } else {
      return plotManager().getPlot(plotNames[plotnum]);
    }
  }

  /// runtime error handling

  public void runtimeError(final org.nlogo.api.JobOwner owner, final org.nlogo.nvm.Context context,
                           final org.nlogo.nvm.Instruction instruction, final Exception ex) {
    // this method is called from the job thread, so we need to switch over
    // to the event thread.  but in the error dialog we want to be able to
    // show the original thread in which it happened, so we hang on to the
    // current thread before switching - ST 7/30/04
    final Thread thread = Thread.currentThread();
    org.nlogo.awt.EventQueue.invokeLater
        (new Runnable() {
          public void run() {
            runtimeErrorPrivate(owner, context, instruction, thread, ex);
          }
        });
  }

  private void runtimeErrorPrivate(org.nlogo.api.JobOwner owner, final org.nlogo.nvm.Context context,
                                   final org.nlogo.nvm.Instruction instruction,
                                   final Thread thread, final Exception ex) {
    // halt, or at least turn graphics back on if they were off
    if (ex instanceof org.nlogo.nvm.HaltException &&
        ((org.nlogo.nvm.HaltException) ex).haltAll()) {
      halt(); // includes turning graphics back on
    } else if (!(owner instanceof MonitorWidget)) {
      world().displayOn(true);
    }
    // tell the world!
    if (!(ex instanceof org.nlogo.nvm.HaltException)) {
      int[] posAndLength;

      // check to see if the error occurred inside a "run" or "runresult" instruction;
      // if so, report the error as having occurred there - ST 5/7/03
      org.nlogo.api.SourceOwner sourceOwner = context.activation.procedure.owner();
      if (instruction.token() == null) {
        posAndLength = new int[]{-1, 0};
      } else {
        posAndLength = instruction.getPositionAndLength();
      }
      new org.nlogo.window.Events.RuntimeErrorEvent
          (owner, sourceOwner, posAndLength[0], posAndLength[1])
          .raiseLater(this);
    }
    // MonitorWidgets always immediately restart their jobs when a runtime error occurs,
    // but we don't want to just stream errors to the command center, so let's not print
    // anything to the command center, and assume that someday MonitorWidgets will do
    // their own user notification - ST 12/16/01
    if (!(owner instanceof MonitorWidget ||
        ex instanceof org.nlogo.nvm.HaltException)) {
      // It doesn't seem like we should need to use invokeLater() here, because
      // we're already on the event thread.  But without using it, at least on
      // Mac 142U1DP3 (and maybe other Mac VMs, and maybe other platforms too,
      // I don't know), the error dialog didn't wind up with the keyboard focus
      // if the Code tab came forward... probably because something that
      // the call to select() in ProceduresTab was doing was doing invokeLater()
      // itself?  who knows... in any case, this seems to fix it - ST 7/30/04
      org.nlogo.awt.EventQueue.invokeLater
          (new Runnable() {
            public void run() {
              RuntimeErrorDialog.show("Runtime Error", context, instruction, thread, ex);
            }
          });
    }
  }

  /// keep track of model name

  /**
   * sets new model name and type, and, if necessary, disconnects
   * HubNetManager. This must be done at BeforeLoadEvent time, because the
   * model name needs to be available for setting titles and so on by the
   * time we handle LoadBeginEvent.
   */
  public void handle(org.nlogo.window.Events.BeforeLoadEvent e) {
    setPeriodicUpdatesEnabled(false);
    if (e.modelPath.isDefined()) {
      setModelPath(e.modelPath.get());
    } else {
      setModelPath(null);
    }
    setModelType(e.modelType);
    if (hubNetManager().isDefined()) {
      hubNetManager().get().disconnect();
    }
    jobManager.haltSecondary();
    jobManager.haltPrimary();
    getExtensionManager().reset();
    fileManager().handleModelChange();
    previewCommands_$eq(PreviewCommands$.MODULE$.DEFAULT());
    clearDrawing();
    viewManager.resetMouseCors();
    displaySwitchOn(true);
    setProcedures(new HashMap<String, Procedure>());
    lastTicksListenersHeard = -1.0;
    plotManager().forgetAll();
  }

  /**
   * sets new model name and type after a save. Once a model is saved,
   * it becomes TYPE_NORMAL. We don't actually handle the event, because
   * it's important that this get sequenced correctly with stuff in
   * App.handle(). Yuck.
   */
  public void modelSaved(String newModelPath) {
    setModelPath(newModelPath);
    setModelType(ModelTypeJ.NORMAL());
  }

  public void handle(org.nlogo.window.Events.AboutToQuitEvent e) {
    if (hubNetManager().isDefined()) {
      hubNetManager().get().disconnect();
    }
  }

  @Override
  public void hubNetRunning_$eq(boolean running) {
    if (this.hubNetRunning() != running) {
      if (running) {
        viewManager.add(hubNetManager().get());
      } else {
        viewManager.remove(hubNetManager().get());
      }
    }

    super.hubNetRunning_$eq(running);
    hubNetControlCenterAction.setEnabled(hubNetRunning());
  }

  public final javax.swing.Action hubNetControlCenterAction =
      new javax.swing.AbstractAction(I18N.guiJ().get("menu.tools.hubNetControlCenter")) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          hubNetManager().get().showControlCenter();
        }
      };

  public final javax.swing.Action switchTo3DViewAction =
      new javax.swing.AbstractAction(I18N.guiJ().get("menu.tools.3DView.switch")) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          open3DView();
        }
      };

  /// preview commands & aggregate

  public void handle(org.nlogo.window.Events.LoadModelEvent e) {
    loadFromModel(e.model);
    ArrayList<org.nlogo.core.Shape> shapes = new ArrayList<org.nlogo.core.Shape>();
    scala.collection.Iterator<? extends org.nlogo.core.Shape.VectorShape> shapeIterator = e.model.turtleShapes().iterator();
    while (shapeIterator.hasNext()) {
      shapes.add(ShapeConverter.baseVectorShapeToVectorShape(shapeIterator.next()));
    }
    world().turtleShapes().replaceShapes(shapes);
    ArrayList<org.nlogo.core.Shape> linkShapes = new ArrayList<org.nlogo.core.Shape>();
    scala.collection.Iterator<? extends org.nlogo.core.Shape.LinkShape> linkShapeIterator = e.model.linkShapes().iterator();
    while (linkShapeIterator.hasNext()) {
      linkShapes.add(ShapeConverter.baseLinkShapeToLinkShape(linkShapeIterator.next()));
    }
    world().linkShapes().replaceShapes(linkShapes);
  }

  public void snapOn(boolean snapOn) {
    this.snapOn = snapOn;
  }

  public boolean snapOn() {
    return this.snapOn;
  }

  @Override
  public String getSource(String filename)
      throws java.io.IOException {

    String source = null;
    if (externalFileManager != null) {
      source = externalFileManager.getSource(filename);
    }
    if (source == null) {
      source = super.getSource(filename);
    }
    return source;
  }

  public void logCustomMessage(String msg) {
    Logger.logCustomMessage(msg);
  }

  public void logCustomGlobals(Seq<Tuple2<String, String>> nameValuePairs) {
    Logger.logCustomGlobals(nameValuePairs);
  }

}
