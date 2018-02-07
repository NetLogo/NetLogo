// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.util.concurrent.atomic.AtomicReference;

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
import org.nlogo.api.ControlSet;
import org.nlogo.api.FileIO$;
import org.nlogo.api.JobOwner;
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
import org.nlogo.nvm.DisplayStatus;
import org.nlogo.nvm.JobManagerOwner;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Workspace;
import org.nlogo.workspace.SwitchModel;
import org.nlogo.workspace.ModelCompiledFailure;
import org.nlogo.workspace.ModelCompiledSuccess;

import java.util.HashMap;
import java.util.ArrayList;

public abstract strictfp class GUIWorkspace // can't be both abstract and strictfp
    extends GUIWorkspaceScala
    implements
    org.nlogo.window.Event.LinkChild,
    org.nlogo.window.Events.AboutToQuitEvent.Handler,
    org.nlogo.window.Events.AddJobEvent.Handler,
    org.nlogo.window.Events.AfterLoadEvent.Handler,
    org.nlogo.window.Events.BeforeLoadEvent.Handler,
    org.nlogo.window.Events.JobStoppingEvent.Handler,
    org.nlogo.window.Events.RemoveAllJobsEvent.Handler,
    org.nlogo.window.Events.RemoveJobEvent.Handler,
    org.nlogo.window.Events.AddSliderConstraintEvent.Handler,
    org.nlogo.window.Events.RemoveConstraintEvent.Handler,
    org.nlogo.window.Events.AddBooleanConstraintEvent.Handler,
    org.nlogo.window.Events.AddChooserConstraintEvent.Handler,
    org.nlogo.window.Events.AddInputBoxConstraintEvent.Handler,
    org.nlogo.window.Events.CompiledEvent.Handler,
    org.nlogo.api.DrawingInterface {

  public enum KioskLevel {NONE, MODERATE}

  public final KioskLevel kioskLevel;

  private final java.awt.Component linkParent;
  private WidgetContainer widgetContainer = null;
  public GLViewManagerInterface glView = null;

  private PeriodicUpdater periodicUpdater;
  private javax.swing.Timer repaintTimer;
  private Lifeguard lifeguard;

  public GUIWorkspace(WorkspaceConfig config) {
    super(config);
    this.kioskLevel = config.kioskLevel();
    this.linkParent = config.linkParent();
    hubNetControlCenterAction.setEnabled(false);

    viewManager().setPrimary(view());
    viewWidget().settings().addPropertyChangeListener(viewManager());
    viewWidget().settings().addPropertyChangeListener(viewWidget());

    periodicUpdater = new PeriodicUpdater(jobManager());
    periodicUpdater.start();
    world().trailDrawer(this);

    // ensure that any skipped frames get painted eventually
    javax.swing.Action repaintAction =
      new javax.swing.AbstractAction() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (displayStatusRef().get().shouldRender(false) && !jobManager().anyPrimaryJobs()) {
            viewManager().paintImmediately(world().observer().updatePosition());
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
          if (jobManager().anyPrimaryJobs()) {
            jobManager().pokePrimaryJobs();
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
    if (glView != null)
      viewWidget().settings().addPropertyChangeListener(glView);
    if (this.glView != null)
      viewWidget().settings().removePropertyChangeListener(this.glView);
    this.glView = glView;
  }

  public GLViewManagerInterface glView() {
    return this.glView;
  }

  public void stamp(org.nlogo.api.Agent agent, boolean erase) {
    view().renderer().prepareToPaint(view(), view().renderer().trailDrawer().getWidth(), view().renderer().trailDrawer().getHeight());
    view().renderer().trailDrawer().stamp(agent, erase);
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

  public void exportDrawing(String filename, String format)
    throws java.io.IOException {
    FileIO$.MODULE$.writeImageFile(view().renderer().trailDrawer().getAndCreateDrawing(true), filename, format);
  }

  public java.awt.image.BufferedImage getAndCreateDrawing() {
    return getAndCreateDrawing(true);
  }

  public java.awt.image.BufferedImage getAndCreateDrawing(boolean dirty) {
    return view().renderer().trailDrawer().getAndCreateDrawing(dirty);
  }

  @Override
  public void clearDrawing() {
    world().clearDrawing();
    view().renderer().trailDrawer().clearDrawing();
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
    return view().renderer().trailDrawer().sendPixels();
  }

  public void sendPixels(boolean dirty) {
    view().renderer().trailDrawer().sendPixels(dirty);
  }

  @Override
  public void dispose() throws InterruptedException {
    periodicUpdater.stop();

    // These two lines clear any pending periodic updates. Otherwise, when
    // we go to dispose the workspace, we may find it `wait`ing on world,
    // and since the GUIWorkspace is being shut down, nothing will ever call `notify`
    // on world. RG 10/31/17
    setPeriodicUpdatesEnabled(false);
    jobManager().interrupt();

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

  public void waitFor(CommandRunnable runnable)
      throws LogoException {
    ThreadUtils.waitFor(world(), runnable);
  }

  public <T> T waitForResult(ReporterRunnable<T> runnable)
      throws LogoException {
    return ThreadUtils.waitForResult(world(), runnable);
  }

  public void waitForQueuedEvents()
      throws LogoException {
    ThreadUtils.waitForQueuedEvents(world());
  }

  /// Event.LinkChild stuff

  public Object getLinkParent() {
    return linkParent;
  }

  @Deprecated
  public void resizeView() { }

  public void patchSize(double patchSize) {
    viewWidget().settings().patchSize(patchSize);
  }

  public double patchSize() {
    return world().patchSize();
  }

  public void patchesCreatedNotify() {
    new org.nlogo.window.Events.PatchesCreatedEvent().raise(this);
  }

  public boolean compilerTestingMode() {
    return false;
  }

  @Override
  public org.nlogo.api.WorldPropertiesInterface getPropertiesInterface() {
    return viewWidget().settings();
  }

  public void changeTopology(boolean wrapX, boolean wrapY) {
    world().changeTopology(wrapX, wrapY);
    view().renderer().changeTopology(wrapX, wrapY);
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
    return view().renderer();
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

  public void registerDisplaySwitch(DisplaySwitch displaySwitch) {
    viewManager().registerDisplaySwitch(displaySwitch);
  }

  private boolean dualView;

  public boolean dualView() {
    return dualView;
  }

  public void dualView(boolean on) {
    if (on != dualView) {
      dualView = on;
      if (dualView) {
        view().thaw();
        viewManager().setSecondary(view());
      } else {
        view().freeze();
        viewManager().remove(view());
      }
      viewWidget().setVisible(on);
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
    return viewManager().mouseDown();
  }

  public boolean mouseInside()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents();
    return viewManager().mouseInside();
  }

  public double mouseXCor()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents();
    return viewManager().mouseXCor();
  }

  public double mouseYCor()
      throws LogoException {
    // we must first make sure the event thread has had the
    // opportunity to detect any recent mouse movement - ST 5/3/04
    waitForQueuedEvents();
    return viewManager().mouseYCor();
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

  /// Job manager stuff
  // this is called on the job thread when the engine comes up for air - ST 1/10/07
  @Override
  public void breathe(org.nlogo.nvm.Context context) {
    jobManager().maybeRunSecondaryJobs();
    if (updateMode().equals(UpdateModeJ.CONTINUOUS())) {
      updateManager().pseudoTick();
      updateDisplay(true, false);
    }
    context.job.comeUpForAir.set(updateManager().shouldComeUpForAirAgain());
    notifyListeners();
  }

  // called only from job thread, by such primitives as
  // _exportinterface and _usermessage, which need to make sure the
  // whole UI is up-to-date before proceeding - ST 8/30/07, 3/3/11
  public void updateUI() {
    // this makes the tick counter et al update
    ThreadUtils.waitFor(world(), updateRunner());
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
    updateDisplay(true, force); // haveWorldLockAlready = true
    notifyListeners();
  }

  private double lastTicksListenersHeard = -1.0;

  private void notifyListeners() {
    double ticks = world().tickCounter().ticks();
    if (ticks != lastTicksListenersHeard) {
      lastTicksListenersHeard = ticks;
      listenerManager().tickCounterChanged(ticks);
    }
    listenerManager().possibleViewUpdate();
  }

  @Override
  public void halt() {
    jobManager().interrupt();
    org.nlogo.swing.ModalProgressTask.onUIThread(
      getFrame(), "Halting...",
      new Runnable() {
        public void run() {
          GUIWorkspace.super.halt();
          view().dirty();
          view().repaint();
        }});
  }

  // for notification of a changed shape
  public void shapeChanged(org.nlogo.core.Shape shape) {
    viewManager().shapeChanged(shape);
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
      glView.open(compiler().dialect().is3D());
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
    return view().renderer().trailDrawer().colors();
  }

  public boolean isDirty() {
    return view().renderer().trailDrawer().isDirty();
  }

  public boolean isBlank() {
    return view().renderer().trailDrawer().isBlank();
  }

  public void markClean() {
    view().renderer().trailDrawer().markClean();
  }

  public void markDirty() {
    view().renderer().trailDrawer().markDirty();
  }

  public int getWidth() {
    return view().renderer().trailDrawer().getWidth();
  }

  public int getHeight() {
    return view().renderer().trailDrawer().getHeight();
  }

  public void readImage(java.io.InputStream is) throws java.io.IOException {
    view().renderer().trailDrawer().readImage(is);
  }

  public void readImage(java.awt.image.BufferedImage image) throws java.io.IOException {
    view().renderer().trailDrawer().readImage(image);
  }

  public void rescaleDrawing() {
    view().renderer().trailDrawer().rescaleDrawing();
  }

  public void drawLine(double x0, double y0, double x1, double y1,
                       Object color, double size, String mode) {
    view().renderer().trailDrawer().drawLine
        (x0, y0, x1, y1, color, size, mode);
    if (hubNetManager().isDefined()) {
      hubNetManager().get().sendLine(x0, y0, x1, y1, color, size, mode);
    }
  }

  public void setColors(int[] colors) {
    view().renderer().trailDrawer().setColors(colors);
  }

  public Object getDrawing() {
    return view().renderer().trailDrawer().getDrawing();
  }

  // called on job thread, but without world lock - ST 9/12/07
  public void ownerFinished(org.nlogo.api.JobOwner owner) {
    new org.nlogo.window.Events.JobRemovedEvent(owner).raiseLater(this);
    if (owner.ownsPrimaryJobs()) {
      updateManager().reset();
      updateDisplay(false, false);
    }
  }

  public void handle(org.nlogo.window.Events.AddJobEvent e) {
    org.nlogo.api.JobOwner owner = e.owner;
    AgentSet agents = e.agents;
    if (owner instanceof JobWidget &&
        agents == null) {
      JobWidget widget = (JobWidget) owner;
      if (widget.useAgentClass()) {
        agents = world().agentSetOfKind(widget.kind());
      }
    }
    if (owner.ownsPrimaryJobs()) {
      if (e.procedure != null) {
        jobManager().addJob(owner, agents, e.procedure);
      } else {
        new org.nlogo.window.Events.JobRemovedEvent(owner).raiseLater(this);
      }
    } else {
      jobManager().addSecondaryJob(owner, agents, e.procedure);
    }
  }

  public void handle(org.nlogo.window.Events.RemoveJobEvent e) {
    org.nlogo.api.JobOwner owner = e.owner;
    if (owner.ownsPrimaryJobs()) {
      jobManager().finishJobs(owner);
    } else {
      jobManager().finishSecondaryJobs(owner);
    }
  }

  public void handle(org.nlogo.window.Events.JobStoppingEvent e) {
    jobManager().stoppingJobs(e.owner);
  }

  public void handle(org.nlogo.window.Events.RemoveAllJobsEvent e) {
    jobManager().haltSecondary();
    jobManager().haltPrimary();
  }

  public void handle(org.nlogo.window.Events.AddBooleanConstraintEvent e) {
    BooleanConstraint con =
        new BooleanConstraint(e.defaultValue);

    // now we set the constraint in the observer, so that it is enforced.
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());

    if (index != -1) {
      world().observer().setConstraint(index, con);
    }
  }

  public void handle(org.nlogo.window.Events.AddInputBoxConstraintEvent e) {
    // now we set the constraint in the observer, so that it is enforced.
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());

    if (index != -1) {
      world().observer().setConstraint(index, e.constraint);
    }
  }

  public void handle(org.nlogo.window.Events.AddChooserConstraintEvent e) {
    // now we set the constraint in the observer, so that it is enforced.
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());

    if (index != -1) {
      world().observer().setConstraint(index, e.constraint);
    }
  }


  public void handle(org.nlogo.window.Events.AddSliderConstraintEvent e) {
    try {
      SliderConstraint con = SliderConstraint.makeSliderConstraint
          (world().observer(), e.minSpec, e.maxSpec, e.incSpec, e.value, e.slider.name(), this, compilerServices());
      e.slider.removeAllErrors();
      e.slider.setSliderConstraint(con);
      // now we set the constraint in the observer, so that it is enforced.
      int index = world().observerOwnsIndexOf(e.varname.toUpperCase());
      if (index != -1) {
        world().observer().setConstraint(index, con);
      }
    } catch (SliderConstraint.ConstraintExceptionHolder ex) {
      for (SliderConstraint.SliderConstraintException cce :
             scala.collection.JavaConversions.asJavaIterable(ex.getErrors())) {
        e.slider.error((Object) cce.spec().fieldName(), (java.lang.Exception) cce);
      }
    }
  }

  public void handle(org.nlogo.window.Events.RemoveConstraintEvent e) {
    int index = world().observerOwnsIndexOf(e.varname.toUpperCase());
    if (index != -1) {
      world().observer().setConstraint(index, null);
    }
  }

  public void handle(org.nlogo.window.Events.CompiledEvent e) {
    if (e.program != null)
      messageCenter().send(new ModelCompiledSuccess(e.program));
    else if (e.error != null)
      messageCenter().send(new ModelCompiledFailure(e.error));
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
          (world(), new Runnable() {
            public void run() {
              event.raise(GUIWorkspace.this);
            }
          });
    } else {
      event.raise(this);
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
    messageCenter().send(new SwitchModel(e.modelPath, e.modelType));
    clearDrawing();
    viewManager().resetMouseCors();
    enableDisplayUpdates();
    lastTicksListenersHeard = -1.0;
  }

  /**
   * sets new model name and type after a save. Once a model is saved,
   * it becomes TYPE_NORMAL. We don't actually handle the event, because
   * it's important that this get sequenced correctly with stuff in
   * App.handle(). Yuck.
   */
  public void modelSaved(String newModelPath) {
    modelTracker().setModelPath(newModelPath);
    modelTracker().setModelType(ModelTypeJ.NORMAL());
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
        viewManager().add(hubNetManager().get());
      } else {
        viewManager().remove(hubNetManager().get());
      }
    }

    super.hubNetRunning_$eq(running);
    hubNetControlCenterAction.setEnabled(hubNetRunning());
  }

  public final javax.swing.Action hubNetControlCenterAction = new HubNetControlCenterAction(this);

  final javax.swing.Action switchTo3DViewAction =
    new javax.swing.AbstractAction(I18N.guiJ().get("menu.tools.3DView.switch")) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        open3DView();
      }
    };

  public javax.swing.Action switchTo3DViewAction() {
    return switchTo3DViewAction;
  }
}
