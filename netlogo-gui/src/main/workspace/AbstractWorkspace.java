// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import scala.collection.mutable.WeakHashMap;
import org.nlogo.agent.Agent;
import org.nlogo.api.*;
import org.nlogo.core.CompilerException;
import org.nlogo.core.Femto;
import org.nlogo.core.FileModeJ;
import org.nlogo.core.File;
import org.nlogo.core.CompilerException;
import org.nlogo.core.Token;
import org.nlogo.core.TokenType;
import org.nlogo.core.UpdateMode;
import org.nlogo.core.UpdateModeJ;
import org.nlogo.agent.ImporterJ;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EditorWorkspace;
import org.nlogo.nvm.FileManager;
import org.nlogo.nvm.Job;
import org.nlogo.nvm.JobManagerInterface;
import org.nlogo.nvm.LoggingWorkspace;
import org.nlogo.nvm.MutableLong;
import org.nlogo.nvm.PresentationCompilerInterface;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.Workspace;

public abstract strictfp class AbstractWorkspace
    implements Workspace,
    EditorWorkspace,
    ExtendableWorkspace,
    LoggingWorkspace,
    org.nlogo.api.LogoThunkFactory,
    org.nlogo.api.HubNetWorkspaceInterface {

  /// globals
  /// (some of these probably should be changed not to be public - ST 12/11/01)

  public final org.nlogo.agent.World _world;

  public final org.nlogo.nvm.JobManagerInterface jobManager;
  protected final Evaluator evaluator;
  protected final ExtensionManager extensionManager;

  public abstract WeakHashMap<Job, WeakHashMap<Agent, WeakHashMap<Command, MutableLong>>> lastRunTimes();

  //public final WorldLoader worldLoader ;

  /// startup

  protected AbstractWorkspace(org.nlogo.agent.World world) {
    this._world = world;
    evaluator = new Evaluator(this);
    jobManager = Femto.getJ(JobManagerInterface.class, "org.nlogo.job.JobManager",
        new Object[]{this, world});
    extensionManager = new ExtensionManager(this, new JarLoader(this));
  }

  public org.nlogo.workspace.ExtensionManager getExtensionManager() {
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
   * Shuts down the background thread associated with this workspace,
   * allowing resources to be freed.
   */
  public void dispose()
      throws InterruptedException {
    jobManager.die();
    getExtensionManager().reset();
  }

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

  /// isApp/isApplet

  // Note that if using the embedding API, both isApp and isApplet are false.

  private static boolean isApp = false;

  public static boolean isApp() {
    return isApp;
  }

  public static void isApp(boolean isApp) {
    AbstractWorkspace.isApp = isApp;
  }

  private static boolean isApplet = true;

  public static boolean isApplet() {
    return isApplet;
  }

  public static void isApplet(boolean isApplet) {
    AbstractWorkspace.isApplet = isApplet;
  }

  /// hubnet



  public org.nlogo.api.WorldPropertiesInterface getPropertiesInterface() {
    return null;
  }

  /// model name utilities

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

  public abstract scala.collection.immutable.ListMap<String, Procedure> procedures();
  public abstract void setProcedures(scala.collection.immutable.ListMap<String, Procedure> procedures);

  public abstract void init();

  @Override
  public abstract PresentationCompilerInterface compiler();

  public abstract AggregateManagerInterface aggregateManager();

  /// methods that may be called from the job thread by prims

  public void joinForeverButtons(org.nlogo.agent.Agent agent) {
    jobManager.joinForeverButtons(agent);
  }

  public void addJobFromJobThread(org.nlogo.nvm.Job job) {
    jobManager.addJobFromJobThread(job);
  }

  public abstract void magicOpen(String name);

  // this is used to cache the compiled code used by the "run"
  // and "runresult" prims - ST 6/7/07
  public WeakHashMap<String, Procedure> codeBits =
      new WeakHashMap<String, Procedure>();

  public Procedure compileForRun(String source, org.nlogo.nvm.Context context,
                                 boolean reporter)
      throws CompilerException {
    String key = source + "@" + context.activation.procedure().args().size() +
        "@" + context.agentBit;
    scala.Option<Procedure> storedProc = codeBits.get(key);
    if (storedProc.isEmpty()) {
      Procedure proc = evaluator.compileForRun(source, context, reporter);
      codeBits.put(key, proc);
      return proc;
    } else {
      return storedProc.get();
    }
  }

  /// misc

  // we shouldn't need "Workspace." lampsvn.epfl.ch/trac/scala/ticket/1409 - ST 4/6/09
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
    enablePeriodicRendering();
  }

  // called by _display from job thread
  public abstract void requestDisplayUpdate(boolean force);

  // called when the engine comes up for air
  public abstract void breathe();

  public void breathe(org.nlogo.nvm.Context context) {
    breathe();
  }

  /// output

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
      org.nlogo.api.Exceptions.handle(e);
    }
  }

  public abstract void clearDrawing();

  protected abstract class FileImporter {
    public String filename;

    FileImporter(String filename) {
      this.filename = filename;
    }

    public abstract void doImport(org.nlogo.core.File reader)
        throws java.io.IOException;
  }

  public abstract void clearAll();

  protected abstract org.nlogo.agent.ImporterJ.ErrorHandler importerErrorHandler();

  public void importDrawing(String filename)
      throws java.io.IOException {
    doImport
        (new FileImporter(filename) {
          @Override
          public void doImport(org.nlogo.core.File file)
              throws java.io.IOException {

            importDrawing(file);
          }
        });
  }

  protected abstract void importDrawing(org.nlogo.core.File file)
      throws java.io.IOException;

  // overridden in subclasses - ST 9/8/03, 3/1/11
  public void doImport(BufferedReaderImporter importer)
      throws java.io.IOException {
    org.nlogo.core.File file = new org.nlogo.api.LocalFile(importer.filename());
    try {
      file.open(org.nlogo.core.FileModeJ.READ());
      importer.doImport(file.reader());
    } finally {
      try {
        file.close(false);
      } catch (java.io.IOException ex2) {
        org.nlogo.api.Exceptions.ignore(ex2);
      }
    }
  }


  // protected because GUIWorkspace will override - ST 9/8/03
  protected void doImport(FileImporter importer)
      throws java.io.IOException {
    final org.nlogo.core.File newFile;

    if (AbstractWorkspace.isApplet()) {
      newFile = new org.nlogo.api.RemoteFile(importer.filename);
    } else {
      newFile = new org.nlogo.api.LocalFile(importer.filename);
    }

    importer.doImport(newFile);
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

  /// BehaviorSpace

  public org.nlogo.api.MersenneTwisterFast auxRNG() {
    return _world.auxRNG();
  }

  public org.nlogo.api.MersenneTwisterFast mainRNG() {
    return _world.mainRNG();
  }
}
