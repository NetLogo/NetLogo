// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.nlogo.agent.Agent;
import org.nlogo.api.*;
import org.nlogo.agent.Importer;
import org.nlogo.agent.ImporterJ;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.FileManager;
import org.nlogo.nvm.Job;
import org.nlogo.nvm.JobManagerInterface;
import org.nlogo.nvm.MutableLong;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Workspace;
import org.nlogo.util.Femto;

public abstract strictfp class AbstractWorkspace
    implements Workspace,
    org.nlogo.api.LogoThunkFactory {

  /// globals
  /// (some of these probably should be changed not to be public - ST 12/11/01)

  public final org.nlogo.agent.World world;

  public org.nlogo.agent.World world() {
    return world;
  }

  protected final DefaultFileManager fileManager;

  public FileManager fileManager() {
    return fileManager;
  }

  private org.nlogo.nvm.Tracer tracer = null;

  public org.nlogo.nvm.Tracer profilingTracer() {
    return tracer;
  }

  public boolean profilingEnabled() {
    return tracer != null;
  }

  public void setProfilingTracer(org.nlogo.nvm.Tracer tracer) {
    this.tracer = tracer;
  }

  public final org.nlogo.nvm.JobManagerInterface jobManager;
  protected final Evaluator evaluator;
  protected final ExtensionManager extensionManager;

  private final WeakHashMap<Job, WeakHashMap<Agent, WeakHashMap<Command, MutableLong>>> lastRunTimes =
      new WeakHashMap<Job, WeakHashMap<Agent, WeakHashMap<Command, MutableLong>>>(); // public for _every

  public WeakHashMap<Job, WeakHashMap<Agent, WeakHashMap<Command, MutableLong>>> lastRunTimes() {
    return lastRunTimes;
  }

  // for _thunkdidfinish (says that a thunk finished running without having stop called)
  private final WeakHashMap<Activation, Boolean> completedActivations = new WeakHashMap<Activation, Boolean>();

  public WeakHashMap<Activation, Boolean> completedActivations() {
    return completedActivations;
  }

  /**
   * name of the currently loaded model. Will be null if this is a new
   * (unsaved) model. To get a version for display to the user, see
   * modelNameForDisplay(). This is NOT a full path name, however, it does
   * end in ".nlogo".
   */
  protected String modelFileName;

  /**
   * path to the directory from which the current model was loaded. NetLogo
   * uses this as the default path for file I/O, when reloading models,
   * etc. This is null if this is a new (unsaved) model.
   */
  private String modelDir;

  /**
   * type of the currently loaded model. Certain aspects of NetLogo's
   * behavior depend on this, i.e. whether to force a save-as and so on.
   */
  private ModelType modelType;

  /// startup

  protected AbstractWorkspace(org.nlogo.agent.World world) {
    this.world = world;
    modelType = ModelTypeJ.NEW();
    evaluator = new Evaluator(this);
    world.compiler_$eq(this);
    jobManager = Femto.get(JobManagerInterface.class, "org.nlogo.job.JobManager",
        new Object[]{this, world, world});
    fileManager = new DefaultFileManager(this);
    extensionManager = new ExtensionManager(this);
  }

  public org.nlogo.api.ExtensionManager getExtensionManager() {
    return extensionManager;
  }

  public boolean isExtensionName(String name) {
    return extensionManager.isExtensionName(name);
  }

  public void importExtensionData(String name, List<String[]> data, org.nlogo.api.ImportErrorHandler handler)
      throws org.nlogo.api.ExtensionException {
    extensionManager.importExtensionData(name, data, handler);
  }

  /**
   * Internal use only.
   */
  public abstract boolean compilerTestingMode();

  /**
   * Shuts down the background thread associated with this workspace,
   * allowing resources to be freed.
   */
  public void dispose()
      throws InterruptedException {
    jobManager.die();
    getExtensionManager().reset();
  }

  /// headless?

  public abstract boolean isHeadless();

  /**
   * Displays a warning to the user, and determine whether to continue.
   * The default (non-GUI) implementation is to print the warning and
   * always continue.
   */
  public boolean warningMessage(String message) {
    System.err.println();
    System.err.println("WARNING: " + message);
    System.err.println();

    // always continue.
    return true;
  }

  /// isApp

  // Note that if using the embedding API, isApp is false.

  private static boolean isApp = false;

  public static boolean isApp() {
    return isApp;
  }

  public static void isApp(boolean isApp) {
    AbstractWorkspace.isApp = isApp;
  }

  public org.nlogo.api.WorldPropertiesInterface getPropertiesInterface() {
    return null;
  }

  /// model name utilities

  public void setModelPath(String modelPath) {
    if (modelPath == null) {
      modelFileName = null;
      modelDir = null;
    } else {
      java.io.File file = new java.io.File(modelPath).getAbsoluteFile();
      modelFileName = file.getName();
      modelDir = file.getParent();
      if (modelDir.equals("")) {
        modelDir = null;
      }
      if (modelDir != null) {
        fileManager.setPrefix(modelDir);
      }
    }
  }

  /**
   * attaches the current model directory to a relative path, if necessary.
   * If filePath is an absolute path, this method simply returns it.
   * If it's a relative path, then the current model directory is prepended
   * to it. If this is a new model, the user's platform-dependent home
   * directory is prepended instead.
   */
  public String attachModelDir(String filePath)
      throws java.net.MalformedURLException {
    if (new java.io.File(filePath).isAbsolute()) {
      return filePath;
    }
    String path = getModelPath();
    if (path == null) {
      path = System.getProperty("user.home")
          + java.io.File.separatorChar + "dummy.txt";
    }

    java.net.URL urlForm =
        new java.net.URL
            (toURL(new java.io.File(path)), filePath);

    return new java.io.File(urlForm.getFile()).getAbsolutePath();
  }

  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings("deprecation")
  public static java.net.URL toURL(java.io.File file)
      throws java.net.MalformedURLException {
    return file.toURL();
  }

  /**
   * instantly converts the current model to ModelTypeJ.NORMAL. This is used
   * by the __edit command to enable quick saving of library models. It
   * probably shouldn't be used anywhere else.
   */
  public String convertToNormal()
      throws java.io.IOException {
    java.io.File git = new java.io.File(".git");
    if (!git.exists() || !git.isDirectory()) {
      throw new java.io.IOException("no .git directory found");
    }
    modelType = ModelTypeJ.NORMAL();
    return getModelPath();
  }

  protected void setModelType(ModelType modelType) {
    this.modelType = modelType;
  }

  /**
   * returns the full pathname of the currently loaded model, if any. This
   * may return null in some cases, for instance if this is a new model.
   */
  public String getModelPath() {
    if (modelDir == null || modelFileName == null) {
      return null;
    }
    return modelDir + java.io.File.separatorChar + modelFileName;
  }

  /**
   * returns the name of the file from which the current model was loaded.
   * May be null if, for example, this is a new model.
   */
  public String getModelFileName() {
    return modelFileName;
  }

  /**
   * returns the full path to the directory from which the current model was
   * loaded. May be null if, for example, this is a new model.
   */
  public String getModelDir() {
    return modelDir;
  }

  public ModelType getModelType() {
    return modelType;
  }

  /**
   * whether the user needs to enter a new filename to save this model.
   * We need to do a "save as" if the model is new, from the
   * models library, or converted.
   * <p/>
   * Basically, only normal models can get silently saved.
   */
  public boolean forceSaveAs() {
    return modelType == ModelTypeJ.NEW()
      || modelType == ModelTypeJ.LIBRARY();
  }

  public String modelNameForDisplay() {
    return makeModelNameForDisplay(modelFileName);
  }

  /**
   * converts a model's filename to an externally displayable model name.
   * The argument may be null, the return value will never be.
   * <p/>
   * Package protected for unit testing.
   */
  static String makeModelNameForDisplay(String str) {
    if (str == null) {
      return "Untitled";
    }
    int suffixIndex = str.lastIndexOf(".nlogo");
    if (suffixIndex > 0 && suffixIndex == str.length() - 6) {
      str = str.substring(0, str.length() - 6);
    }
    return str;
  }

  /// methods that may be called from the job thread by prims

  public void joinForeverButtons(org.nlogo.agent.Agent agent) {
    jobManager.joinForeverButtons(agent);
  }

  public void addJobFromJobThread(org.nlogo.nvm.Job job) {
    jobManager.addJobFromJobThread(job);
  }

  public abstract void magicOpen(String name);

  public abstract void changeLanguage();

  // this is used to cache the compiled code used by the "run"
  // and "runresult" prims - ST 6/7/07
  public WeakHashMap<String, Procedure> codeBits =
      new WeakHashMap<String, Procedure>();

  public Procedure compileForRun(String source, org.nlogo.nvm.Context context,
                                 boolean reporter)
      throws CompilerException {
    String key = source + "@" + context.activation.procedure().args.size() +
        "@" + context.agentBit;
    Procedure proc = codeBits.get(key);
    if (proc == null) {
      proc = evaluator.compileForRun(source, context, reporter);
      codeBits.put(key, proc);
    }
    return proc;
  }

  /// misc

  private UpdateMode updateMode = UpdateModeJ.CONTINUOUS();

  public UpdateMode updateMode() {
    return updateMode;
  }

  public void updateMode(UpdateMode updateMode) {
    this.updateMode = updateMode;
  }

  // called from an "other" thread (neither event thread nor job thread)
  public abstract void open(String path)
      throws java.io.IOException, CompilerException, LogoException;

  public abstract void openString(String modelContents)
      throws CompilerException, LogoException;

  public void halt() {
    jobManager.haltPrimary();
    world.displayOn(true);
  }

  // called by _display from job thread
  public abstract void requestDisplayUpdate(boolean force);

  // called when the engine comes up for air
  public abstract void breathe();

  /// output

  public void outputObject(Object object, Object owner,
                           boolean addNewline, boolean readable,
                           OutputDestination destination)
      throws LogoException {
    org.nlogo.agent.OutputObject oo =
        new org.nlogo.agent.OutputObject
            (
                // caption
                owner instanceof org.nlogo.agent.Agent
                    ? Dump.logoObject(owner)
                    : "",
                // message
                (readable && !(owner instanceof org.nlogo.agent.Agent)
                    ? " "
                    : "")
                    + Dump.logoObject(object, readable, false),
                // other
                addNewline, false);
    if (destination == OutputDestinationJ.FILE()) {
      fileManager.writeOutputObject(oo);
    } else {
      sendOutput(oo, destination == OutputDestinationJ.OUTPUT_AREA());
    }
  }

  // called from job thread - ST 10/1/03
  protected abstract void sendOutput(org.nlogo.agent.OutputObject oo,
                                     boolean toOutputArea)
      throws LogoException;

  /// importing

  public void setOutputAreaContents(String text) {
    try {
      clearOutput();
      if (text.length() > 0) {
        sendOutput(new org.nlogo.agent.OutputObject(
            "", text, false, false), true);
      }
    } catch (LogoException e) {
      org.nlogo.util.Exceptions.handle(e);
    }
  }

  public abstract void clearDrawing();

  protected abstract class FileImporter {
    public String filename;

    FileImporter(String filename) {
      this.filename = filename;
    }

    public abstract void doImport(org.nlogo.api.File reader)
        throws java.io.IOException;
  }

  protected void exportInterfaceGlobals(java.io.PrintWriter writer) {
    writer.println(Dump.csv().header("MODEL SETTINGS"));
    scala.collection.Seq<String> globals = world.program().interfaceGlobals();
    writer.println(Dump.csv().variableNameRow(globals));
    Object[] values = new Object[globals.size()];
    int i = 0;
    for (scala.collection.Iterator<String> iter = globals.iterator(); iter.hasNext(); i++) {
      values[i] =
          world.getObserverVariableByName(iter.next());
    }
    writer.println(Dump.csv().dataRow(values));
    writer.println();
  }


  public abstract void clearAll();

  protected abstract org.nlogo.agent.ImporterJ.ErrorHandler importerErrorHandler();

  public void importWorld(String filename)
      throws java.io.IOException {
    // we need to clearAll before we import in case
    // extensions are hanging on to old data. ev 4/10/09
    clearAll();
    doImport
        (new BufferedReaderImporter(filename) {
          @Override
          public void doImport(java.io.BufferedReader reader)
              throws java.io.IOException {
            world.importWorld
                (importerErrorHandler(), AbstractWorkspace.this,
                    stringReader(), reader);
          }
        });
  }

  public void importWorld(java.io.Reader reader)
      throws java.io.IOException {
    // we need to clearAll before we import in case
    // extensions are hanging on to old data. ev 4/10/09
    clearAll();
    world.importWorld
        (importerErrorHandler(), AbstractWorkspace.this,
            stringReader(), new java.io.BufferedReader(reader));
  }

  private final ImporterJ.StringReader stringReader() {
    return new ImporterJ.StringReader() {
      public Object readFromString(String s)
          throws Importer.StringReaderException {
        try {
          return compiler().readFromString
            (s, world, extensionManager);
        } catch (CompilerException ex) {
          throw new Importer.StringReaderException
              (ex.getMessage());
        }
      }
    };
  }

  public void importDrawing(String filename)
      throws java.io.IOException {
    doImport
        (new FileImporter(filename) {
          @Override
          public void doImport(org.nlogo.api.File file)
              throws java.io.IOException {

            importDrawing(file);
          }
        });
  }

  protected abstract void importDrawing(org.nlogo.api.File file)
      throws java.io.IOException;

  // overridden in subclasses - ST 9/8/03, 3/1/11
  public void doImport(BufferedReaderImporter importer)
      throws java.io.IOException {
    org.nlogo.api.File file = new org.nlogo.api.LocalFile(importer.filename());
    try {
      file.open(org.nlogo.api.FileModeJ.READ());
      importer.doImport(file.reader());
    } finally {
      try {
        file.close(false);
      } catch (java.io.IOException ex2) {
        org.nlogo.util.Exceptions.ignore(ex2);
      }
    }
  }


  // protected because GUIWorkspace will override - ST 9/8/03
  protected void doImport(FileImporter importer)
      throws java.io.IOException {
    importer.doImport(new org.nlogo.api.LocalFile(importer.filename));
  }

  /// exporting

  public String guessExportName(String defaultName) {
    String modelName = getModelFileName();
    int index;

    if (modelName == null) {
      return defaultName;
    }

    index = modelName.lastIndexOf(".nlogo");
    if (index > -1) {
      modelName = modelName.substring(0, index);
    }

    return modelName + " " + defaultName;
  }

  public org.nlogo.api.File exportBehaviors(String filename,
                                            String experimentName,
                                            boolean includeHeader)
      throws java.io.IOException {
    org.nlogo.api.File file = new org.nlogo.api.LocalFile(filename);
    file.open(org.nlogo.api.FileModeJ.WRITE());
    if (includeHeader) {
      org.nlogo.agent.AbstractExporter.exportHeader
          (file.getPrintWriter(), "BehaviorSpace", modelFileName, experimentName);
      file.getPrintWriter().flush(); // perhaps not necessary, but just in case... - ST 2/23/05
    }
    return file;
  }

  /// BehaviorSpace

  private int _behaviorSpaceRunNumber = 0;

  public int behaviorSpaceRunNumber() {
    return _behaviorSpaceRunNumber;
  }

  public void behaviorSpaceRunNumber(int n) {
    _behaviorSpaceRunNumber = n;
  }

  public String getSource(String filename)
      throws java.io.IOException {
    // when we stick a string into a JTextComponent, \r\n sequences
    // on Windows will get translated to just \n.  This is a problem
    // because when an error occurs we want to highlight the location
    // using the token location information recorded by the tokenizer,
    // but the removal of the \r characters will throw off that information.
    // So we do the stripping of \r here, *before* we run the tokenizer,
    // and that avoids the problem. - ST 9/14/04
    return new org.nlogo.api.LocalFile(filename).readFile().replaceAll("\r\n", "\n");
  }

  public void loadWorld(scala.collection.Seq<String> strings, WorldLoaderInterface worldInterface) {
    WorldLoader loader = new WorldLoader();
    loader.load(strings, worldInterface);
  }

  public org.nlogo.util.MersenneTwisterFast auxRNG() {
    return world.auxRNG;
  }

  public org.nlogo.util.MersenneTwisterFast mainRNG() {
    return world.mainRNG;
  }

  public Object readNumberFromString(String source)
      throws CompilerException {
    return compiler().readNumberFromString
      (source, world, getExtensionManager());
  }

  public void checkReporterSyntax(String source)
      throws CompilerException {
    compiler().checkReporterSyntax
        (source, world.program(), procedures(), getExtensionManager(), false);
  }

  public void checkCommandSyntax(String source)
      throws CompilerException {
    compiler().checkCommandSyntax
        (source, world.program(), procedures(), getExtensionManager(), false);
  }

  public boolean isConstant(String s) {
    try {
      compiler().readFromString(s);
      return true;
    }
    catch(CompilerException e) {
      return false;
    }
  }

  public boolean isValidIdentifier(String s) {
    return compiler().isValidIdentifier(s);
  }

  public boolean isReporter(String s) {
    return compiler().isReporter(s, world.program(), procedures(), getExtensionManager());
  }

  public Token[] tokenizeForColorization(String s) {
    return compiler().tokenizeForColorization
      (s, getExtensionManager());
  }

  public Token getTokenAtPosition(String s, int pos) {
    return compiler().getTokenAtPosition(s, pos);
  }

  public java.util.Map<String, java.util.List<Object>> findProcedurePositions(String source) {
    return compiler().findProcedurePositions(source);
  }

  public abstract org.nlogo.nvm.CompilerInterface compiler();

  public LogoException lastLogoException() {
    return null;
  }

  public void clearLastLogoException() { }

  public void lastLogoException_$eq(LogoException e) { }

}
