package org.nlogo.headless;

// Note that in the javadoc we distribute to the public, this class
// is included, but Workspace and AbstractWorkspace are not, so if you
// want to document a method for the public, you need to override
// that method here and document it here.  It's fine for the overriding
// method to simply call super(). - ST 6/1/05

import java.util.Collections;
import java.util.StringTokenizer;

import org.nlogo.agent.Agent;
import org.nlogo.agent.Observer;
import org.nlogo.api.JobOwner;
import org.nlogo.api.Version;
import org.nlogo.api.RendererInterface;
import org.nlogo.api.Perspective;
import org.nlogo.api.WorldDimensions;
import org.nlogo.api.WorldDimensions3D;
import org.nlogo.api.AggregateManagerInterface;
import org.nlogo.api.ModelReader;
import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.api.HubNetInterface;
import org.nlogo.agent.World;
import org.nlogo.agent.World3D;
import org.nlogo.nvm.CompilerResults;
import org.nlogo.nvm.LabInterface;
import org.nlogo.plot.Plot;
import org.nlogo.plot.PlotPen;
import org.nlogo.plot.PlotException;
import org.nlogo.nvm.Workspace;
import org.nlogo.nvm.DefaultCompilerServices;
import org.nlogo.nvm.CompilerInterface;
import org.nlogo.workspace.AbstractWorkspace;
import org.nlogo.workspace.AbstractWorkspaceScala;
import org.nlogo.util.Pico;
import org.picocontainer.Parameter;
import org.picocontainer.parameters.ComponentParameter;

/**
 * The primary class for headless (no GUI) operation of NetLogo.
 * <p/>
 * <p>You may create more than one HeadlessWorkspace object.  Multiple
 * instances can operate separately and independently.  (Behind the
 * scenes, this is supported by creating a separate thread for each
 * instance.)
 * <p/>
 * <p>When you are done using a HeadlessWorkspace, you should call its
 * dispose() method.  This will shut down the thread associated with
 * the workspace and allow resources to be freed.
 * <p/>
 * <p>See the "Controlling" section of the NetLogo User Manual
 * for example code.
 */

public strictfp class HeadlessWorkspace
    extends AbstractWorkspaceScala
    implements org.nlogo.workspace.Controllable,
    org.nlogo.workspace.WorldLoaderInterface,
    org.nlogo.api.ViewSettings {

  /**
   * Makes a new instance of NetLogo capable of running a model
   * "headless", with no GUI.
   */
  public static HeadlessWorkspace newInstance() {
    return newInstance(HeadlessWorkspace.class);
  }

  /**
   * If you derive your own subclass of HeadlessWorkspace, you can use this
   * method to instantiate it.
   */
  public static HeadlessWorkspace newInstance(Class<? extends HeadlessWorkspace> subclass) {
    final Pico pico = new Pico();
    pico.addComponent(Version.is3D() ? World3D.class : World.class);
    pico.addScalaObject("org.nlogo.compiler.Compiler");
    pico.add("org.nlogo.sdm.AggregateManagerLite");
    pico.add("org.nlogo.render.Renderer");
    pico.add(HubNetInterface.class,
        "org.nlogo.hubnet.server.HeadlessHubNetManager",
        new Parameter[]{new ComponentParameter()});
    pico.addComponent(subclass);
    AbstractWorkspace.HubNetManagerFactory hubNetManagerFactory = new AbstractWorkspace.HubNetManagerFactory() {
      public HubNetInterface newInstance(AbstractWorkspace workspace) {
        return pico.getComponent(HubNetInterface.class);
      }
    };
    pico.addComponent(hubNetManagerFactory);
    return pico.getComponent(subclass);
  }

  public static LabInterface newLab() {
    Pico pico = new Pico();
    pico.addScalaObject("org.nlogo.compiler.Compiler");
    pico.add("org.nlogo.lab.Lab");
    pico.add("org.nlogo.lab.ProtocolLoader");
    pico.addComponent(DefaultCompilerServices.class);
    return pico.getComponent(LabInterface.class);
  }

  private final AggregateManagerInterface aggregateManager;

  public AggregateManagerInterface aggregateManager() {
    return aggregateManager;
  }

  private final RendererInterface renderer;

  public RendererInterface renderer() {
    return renderer;
  }

  private final CompilerInterface compiler;

  @Override
  public CompilerInterface compiler() {
    return compiler;
  }

  public final JobOwner defaultOwner;

  /**
   * Internal use only. Use newInstance() instead.
   */
  public HeadlessWorkspace(World world,
                           CompilerInterface compiler,
                           RendererInterface renderer,
                           AggregateManagerInterface aggregateManager,
                           AbstractWorkspace.HubNetManagerFactory hubNetManagerFactory) {
    super(world, hubNetManagerFactory);
    AbstractWorkspace.isApplet(false);
    this.compiler = compiler;
    this.renderer = renderer;
    this.aggregateManager = aggregateManager;
    world.trailDrawer(renderer.trailDrawer());
    defaultOwner = new SimpleJobOwner("HeadlessWorkspace", world.mainRNG, Observer.class);
  }

  /**
   * Has a model been opened in this workspace?
   */
  boolean modelOpened = false;

  public final StringBuilder outputAreaBuffer = new StringBuilder();

  /**
   * If true, don't send anything to standard output.
   */
  boolean silent = false;

  void silent(boolean silent) {
    this.silent = silent;
  }

  /**
   * Internal use only.
   */
  @Override
  public boolean isHeadless() {
    return true;
  }

  boolean compilerTestingMode = false;

  /**
   * Internal use only.
   */
  public void setCompilerTestingMode(boolean testing) {
    compilerTestingMode = testing;
  }

  /**
   * Internal use only.
   */
  @Override
  public boolean isCompilerTestingMode() {
    return compilerTestingMode;
  }

  /**
   * Internal use only.
   */
  public void waitFor(org.nlogo.api.CommandRunnable runnable)
      throws LogoException {
    runnable.run();
  }

  /**
   * Internal use only.
   */
  public <T> T waitForResult(org.nlogo.api.ReporterRunnable<T> runnable)
      throws LogoException {
    return runnable.run();
  }

  /**
   * Internal use only.
   */
  public void waitForQueuedEvents() {
  }

  /**
   * Internal use only.
   */
  // batrachomyomachia!
  public static final String TEST_DECLARATIONS =
      "globals [glob1 glob2 glob3 ]\n" +
          "breed [mice mouse]\n " +
          "breed [frogs frog]\n " +
          "breed [nodes node]\n " +
          "directed-link-breed [directed-links directed-link]\n" +
          "undirected-link-breed [undirected-links undirected-link]\n" +
          "turtles-own [tvar]\n" +
          "patches-own [pvar]\n" +
          "mice-own [age fur]\n" +
          "frogs-own [age spots]\n" +
          "directed-links-own [lvar]\n" +
          "undirected-links-own [weight]\n";

  /**
   * Internal use only.
   */
  public void initForTesting(int worldSize)
      throws CompilerException {
    initForTesting(worldSize, "");
  }

  /**
   * Internal use only.
   */
  public void initForTesting(int worldSize, String modelString)
      throws CompilerException {
    if (Version.is3D()) {
      initForTesting(new WorldDimensions3D(
          -worldSize, worldSize, -worldSize, worldSize, -worldSize, worldSize),
          modelString);
    } else {
      initForTesting(-worldSize, worldSize, -worldSize, worldSize, modelString);
    }
  }

  /**
   * Internal use only.
   */
  public void initForTesting(int minPxcor, int maxPxcor, int minPycor, int maxPycor, String source)
      throws CompilerException {
    initForTesting(new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor), source);
  }

  /**
   * Internal use only.
   */
  public void initForTesting(WorldDimensions d, String source)
      throws CompilerException {
    world.turtleShapeList().add(org.nlogo.shape.VectorShape.getDefaultShape());
    world.linkShapeList().add(org.nlogo.shape.LinkShape.getDefaultLinkShape());
    world.createPatches(d);
    CompilerResults results =
        compiler().compileProgram
            (source, world.newProgram(Collections.<String>emptyList()),
                getExtensionManager());
    setProcedures(results.proceduresMap());
    codeBits.clear();
    init();
    world.program(results.program());
    world.realloc();

    // setup some test plots.
    plotManager().forgetAll();
    Plot plot1 = plotManager().newPlot("plot1");
    plot1.createPlotPen("pen1", false);
    plot1.createPlotPen("pen2", false);
    Plot plot2 = plotManager().newPlot("plot2");
    plot2.createPlotPen("pen1", false);
    plot2.createPlotPen("pen2", false);
    plotManager().compileAllPlots();

    clearDrawing();
  }

  public void initForTesting(int minPxcor, int maxPxcor, int minPycor, int maxPycor) {
    initForTesting(new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor));
  }

  /**
   * Internal use only.
   */
  public void initForTesting(org.nlogo.api.WorldDimensions d) {
    world.createPatches(d);
    world.realloc();
    clearDrawing();
  }

  /**
   * Kills all turtles, clears all patch variables, and makes a new patch
   * grid.
   */
  public void setDimensions(WorldDimensions d) {
    world.createPatches(d);
    clearDrawing();
  }

  public void setDimensions(WorldDimensions d, double patchSize) {
    world.patchSize(patchSize);
    if (!compilerTestingMode) {
      world.createPatches(d);
    }
    renderer.resetCache(patchSize());
    clearDrawing();
  }

  // none of these methods should really ever get called
  // headless except getMinimumWidth, since we really
  // don't care about the minimum width of widgets
  // when we're headless.  ev 2/14/07
  public int getMinimumWidth() {
    return 0;
  }

  public int insetWidth() {
    return 0;
  }

  public double computePatchSize(int width, int numPatches) {
    return width / numPatches;
  }

  public int calculateHeight(int worldHeight, double patchSize) {
    return (int) (worldHeight * patchSize);
  }

  public int calculateWidth(int worldWidth, double patchSize) {
    return (int) (worldWidth * patchSize);
  }

  // we're headless, so we don't need to do anything here - ST 4/6/09
  public void resizeView() {
  }

  public void tickCounterLabel(String label) {
  }

  public String tickCounterLabel() {
    return "ticks";
  }

  public void showTickCounter(boolean visible) {
  }

  public boolean showTickCounter() {
    return true;
  }

  public void frameRate(double rate) {
  }

  public double frameRate() {
    return 0.0;
  }

  public int getWidth() {
    return (int) StrictMath.round(world.worldWidth() * world.patchSize());
  }

  public int getHeight() {
    return (int) StrictMath.round(world.worldHeight() * world.patchSize());
  }

  // for now we don't have different view sizes headless.
  public double viewWidth() {
    return world.worldWidth();
  }

  public double viewHeight() {
    return world.worldHeight();
  }

  public void patchSize(double patchSize) {
    world.patchSize(patchSize);
    renderer.resetCache(patchSize());
    renderer.trailDrawer().rescaleDrawing();
  }

  public double patchSize() {
    return world.patchSize();
  }

  public void changeTopology(boolean wrapX, boolean wrapY) {
    world.changeTopology(wrapX, wrapY);
    renderer.changeTopology(wrapX, wrapY);
  }

  public Perspective perspective() {
    return world.observer().perspective();
  }

  public boolean drawSpotlight() {
    return true;
  }

  public boolean renderPerspective() {
    return true;
  }

  public double viewOffsetX() {
    return world.observer().followOffsetX();
  }

  public double viewOffsetY() {
    return world.observer().followOffsetY();
  }

  // we shouldn't need "Workspace." lampsvn.epfl.ch/trac/scala/ticket/1409 - ST 4/6/09
  @Override
  public void updateMode(Workspace.UpdateMode updateMode) {
    // ignore
  }

  private int fontSize = 13;

  public int fontSize() {
    return fontSize;
  }

  public void fontSize(int fontSize) {
    this.fontSize = fontSize;
  }

  public void setSize(int x, int y) {
    // in the gui this sets the size of the widget
    // clearly we ignore it here. ev 2/24/06
  }

  public void clearTurtles() {
    if (!compilerTestingMode) {
      world.clearTurtles();
    }
  }

  /**
   * Internal use only.
   */
  public void createPatchesNotify() {
    // we're headless, do nothing
  }

  /**
   * Internal use only.
   */
  public void inspectAgent(org.nlogo.api.Agent agent, double radius) {
    if (!silent) {
      System.out.println(agent);
    }
  }

  /**
   * Internal use only.
   */
  public void inspectAgent(Class<? extends Agent> agentClass, org.nlogo.agent.Agent agent, double radius) {
    if (!silent) {
      System.out.println(agent);
    }
  }

  public java.awt.image.BufferedImage getAndCreateDrawing() {
    return renderer.trailDrawer().getAndCreateDrawing(true);
  }

  @Override
  public void importDrawing(org.nlogo.api.File file)
      throws java.io.IOException {
    renderer.trailDrawer().importDrawing(file);
  }

  @Override
  public void clearDrawing() {
    world.clearDrawing();
    renderer.trailDrawer().clearDrawing();
  }

  public void exportDrawing(String filename, String format)
      throws java.io.IOException {
    java.io.FileOutputStream stream =
        new java.io.FileOutputStream(new java.io.File(filename));
    javax.imageio.ImageIO.write
        (renderer.trailDrawer().getAndCreateDrawing(true), format, stream);
    stream.close();
  }

  @Override
  public void exportDrawingToCSV(java.io.PrintWriter writer) {
    renderer.trailDrawer().exportDrawingToCSV(writer);
  }

  public void exportOutput(String filename) {
    org.nlogo.api.File file =
        new org.nlogo.api.LocalFile(filename);
    try {
      file.open(org.nlogo.api.File.Mode.WRITE);
      StringTokenizer lines =
          new StringTokenizer(outputAreaBuffer.toString(), "\n");
      while (lines.hasMoreTokens()) {
        // note that since we always use println, we always output a final carriage return
        // even if the TextArea doesn't have one; hmm, bug or feature? let's call it a feature
        file.println(lines.nextToken());
      }
      file.close(true);
    } catch (java.io.IOException ex) {
      try {
        file.close(false);
      } catch (java.io.IOException ex2) {
        org.nlogo.util.Exceptions.ignore(ex2);
      }
    } catch (RuntimeException ex) {
      org.nlogo.util.Exceptions.handle(ex);
    }
  }

  @Override
  public void exportOutputAreaToCSV(java.io.PrintWriter writer) {
    writer.println(org.nlogo.api.Dump.csv.encode("OUTPUT"));
    org.nlogo.api.Dump.csv.stringToCSV(writer, outputAreaBuffer.toString());
  }

  /**
   * Internal use only.
   */
  // called from job thread - ST 10/1/03
  public void clearOutput() {
    outputAreaBuffer.setLength(0);
  }

  /// world importing error handling

  private org.nlogo.agent.Importer.ErrorHandler importErrorHandler =
      new org.nlogo.agent.Importer.ErrorHandler() {
        public boolean showError(String title, String errorDetails,
                                 boolean fatalError) {
          System.err.println("got a " + (fatalError ? "" : "non") +
              "fatal error " + title + ": " + errorDetails);
          return true;
        }
      };

  /**
   * Internal use only.
   */
  @Override
  protected org.nlogo.agent.Importer.ErrorHandler getImporterErrorHandler() {
    return importErrorHandler;
  }

  /**
   * Internal use only.
   */
  public void setImporterErrorHandler(org.nlogo.agent.Importer.ErrorHandler importErrorHandler) {
    this.importErrorHandler = importErrorHandler;
  }

  /**
   * Get a snapshot of the 2D view.
   */
  public java.awt.image.BufferedImage exportView() {
    return renderer.exportView(this);
  }

  /**
   * Get a snapshot of the 2D view, using an existing BufferedImage
   * object.
   */
  public void getGraphics(java.awt.image.BufferedImage image) {
    java.awt.Graphics2D graphics =
        (java.awt.Graphics2D) image.getGraphics();
    java.awt.Font font = graphics.getFont();
    java.awt.Font newFont =
        new java.awt.Font(font.getName(), font.getStyle(), fontSize());
    graphics.setFont(newFont);

    renderer.exportView(graphics, this);
  }

  /**
   * Internal use only.
   */
  public void paint(java.awt.Graphics2D g) {
    renderer.paint(g, this);
  }

  public void exportView(String filename, String format)
      throws java.io.IOException {
    // there's a form of ImageIO.write that just takes a filename, but
    // if we use that when the filename is invalid (e.g. refers to
    // a directory that doesn't exist), we get an IllegalArgumentException
    // instead of an IOException, so we make our own OutputStream
    // so we get the proper exceptions. - ST 8/19/03
    java.awt.image.BufferedImage image = renderer.exportView(this);
    java.io.FileOutputStream stream =
        new java.io.FileOutputStream(new java.io.File(filename));
    javax.imageio.ImageIO.write(image, format, stream);
    stream.close();
  }

  /**
   * Not implemented.
   */
  public void exportInterface(String filename) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeGraphicsData(java.io.PrintWriter writer) {
    writer.print(renderer.exportView(this).getData());
  }

  /**
   * Internal use only.
   */
  // called from job thread - ST 10/1/03
  @Override
  protected void sendOutput(org.nlogo.agent.OutputObject oo, boolean toOutputArea) {
    // output always goes to stdout in headless mode
    if (!silent) {
      System.out.print(oo.get());
    }

    // we also need to record it if it headed for the Output Area widget
    if (toOutputArea) {
      outputAreaBuffer.append(oo.get());
    }

  }

  /**
   * Internal use only.
   */
  public void ownerFinished(org.nlogo.api.JobOwner owner) {
    // we're headless, do nothing
  }

  /**
   * Internal use only.
   */
  public void updateDisplay(boolean haveWorldLockAlready) {
    // we're headless, do nothing
  }

  /**
   * Internal use only.
   */
  @Override
  public void requestDisplayUpdate(boolean force) {
    if (hubnetManager() != null) {
      hubnetManager().incrementalUpdateFromEventThread();
    }
  }

  /**
   * Internal use only.
   */
  @Override
  public void breathe() {
    // called when engine comes up for air. we're headless, do nothing
  }

  /**
   * Internal use only.
   */
  public void periodicUpdate() {
    // we're headless, do nothing
  }

  /**
   * Internal use only.
   */
  @Override
  public void magicOpen(String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  @Override
  public void changeLanguage() {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  public void openIndex() {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  public void openNext() {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  public void openPrevious() {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  public void startLogging(String properties) {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  public void zipLogFiles(String filename) {
    throw new UnsupportedOperationException();
  }

  /**
   * Internal use only.
   */
  public void deleteLogFiles() {
    throw new UnsupportedOperationException();
  }

  // This lastLogoException stuff is gross.  We should write methods
  // that are declared to throw LogoException, rather than requiring
  // that this variable be checked. - ST 2/28/05

  /**
   * Internal use only.
   */
  public LogoException lastLogoException = null;

  /**
   * Internal use only.
   */
  @Override
  public LogoException lastLogoException() {
    return lastLogoException;
  }

  // this is a blatent hack that makes it possible to test the new stack trace stuff.
  // lastErrorReport gives more information than the regular exception
  // that gets thrown from the command function.
  // -JC 11/16/10
  public ErrorReport lastErrorReport = null;

  /**
   * Internal use only.
   */
  @Override
  public void clearLastLogoException() {
    lastLogoException = null;
  }

  /**
   * Internal use only.
   */
  public void runtimeError(org.nlogo.api.JobOwner owner, org.nlogo.nvm.Context context,
                           org.nlogo.nvm.Instruction instruction, Exception ex) {
    if (ex instanceof org.nlogo.api.LogoException) {
      lastLogoException = (org.nlogo.api.LogoException) ex;
      lastErrorReport = new ErrorReport(owner, context, instruction, ex);
    } else {
      System.err.println("owner: " + owner.displayName());
      org.nlogo.util.Exceptions.handle(ex);
    }
  }

  /// Controlling API methods

  /**
   * Opens a model stored in a file.
   *
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  @Override
  public void open(String path)
      throws java.io.IOException, CompilerException, LogoException {
    setModelPath(path);
    String modelContents = org.nlogo.api.FileIO.file2String(path);
    try {
      openString(modelContents);
    } catch (CompilerException ex) {
      // models with special comment are allowed not to compile
      if (compilerTestingMode &&
          modelContents.startsWith(";; DOESN'T COMPILE IN CURRENT BUILD")) {
        System.out.println("ignored compile error: " + path);
      } else {
        throw ex;
      }
    }
  }

  /**
   * Opens a model stored in a string
   *
   * @param modelContents
   */
  @Override
  public void openString(String modelContents)
      throws CompilerException, LogoException {
    fileManager.handleModelChange();
    new HeadlessModelOpener(this).openFromMap(ModelReader.parseModel(modelContents));
  }

  /**
   * Opens a model stored in a string.
   * Can only be called once per instance of HeadlessWorkspace
   *
   * @param source The complete model, including widgets and so forth,
   *               in the same format as it would be stored in a file.
   */
  public void openFromSource(String source)
      throws CompilerException, LogoException {
    new HeadlessModelOpener(this).openFromMap(ModelReader.parseModel(source));
  }

  /**
   * Runs NetLogo commands and waits for them to complete.
   *
   * @param source The command or commands to run
   * @throws org.nlogo.api.CompilerException
   *                       if the code fails to compile
   * @throws LogoException if the code fails to run
   */
  public void command(String source)
      throws CompilerException, LogoException {
    evaluateCommands(defaultOwner, source, true);
    if (lastLogoException != null) {
      LogoException ex = lastLogoException;
      lastLogoException = null;
      throw ex;
    }
  }

  /**
   * Runs a NetLogo reporter.
   *
   * @param source The reporter to run
   * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.api.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws org.nlogo.api.CompilerException
   *                       if the code fails to compile
   * @throws LogoException if the code fails to run
   */
  public Object report(String source)
      throws CompilerException, LogoException {
    Object result = evaluateReporter(defaultOwner, source, world.observer());
    if (lastLogoException != null) {
      LogoException ex = lastLogoException;
      lastLogoException = null;
      throw ex;
    }
    return result;
  }

  /**
   * Halts all running NetLogo code in this workspace.
   */
  @Override
  public void halt()  // NOPMD pmd doesn't like it when an overriding method onlys call super()
  {
    // we just invoke the method in our superclass, but explicitly
    // writing that (despite pmd's whining) lets us provide
    // javadoc on the method - ST 6/1/05
    super.halt();
  }
}
