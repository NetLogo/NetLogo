// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.Agent;
import org.nlogo.api.CommandRunnable;
import org.nlogo.api.CompilerException;
import org.nlogo.api.CompilerServices;
import org.nlogo.api.HubNetInterface;
import org.nlogo.api.ImporterUser;
import org.nlogo.api.JobOwner;
import org.nlogo.api.LogoException;
import org.nlogo.api.RandomServices;
import org.nlogo.api.ReporterRunnable;

import java.util.Map;
import java.util.WeakHashMap;

public interface Workspace
    extends ImporterUser, JobManagerOwner, CompilerServices, RandomServices {

  enum OutputDestination {NORMAL, OUTPUT_AREA, FILE}

  enum UpdateMode {
    CONTINUOUS, TICK_BASED;

    public int save() {
      switch (this) {
        case CONTINUOUS:
          return 0;
        case TICK_BASED:
          return 1;
        default:
          throw new IllegalStateException();
      }
    }

    public static UpdateMode load(int mode) {
      switch (mode) {
        case 0:
          return CONTINUOUS;
        case 1:
          return TICK_BASED;
        default:
          throw new IllegalStateException();
      }
    }
  }

  org.nlogo.agent.World world();

  Map<String, Procedure> getProcedures();

  void setProcedures(Map<String, Procedure> procedures);

  org.nlogo.api.AggregateManagerInterface aggregateManager();

  void requestDisplayUpdate(boolean force);

  void breathe(); // called when engine comes up for air

  void joinForeverButtons(org.nlogo.agent.Agent agent);

  void addJobFromJobThread(Job job);

  org.nlogo.api.ExtensionManager getExtensionManager();

  void waitFor(CommandRunnable runnable)
      throws LogoException;

  <T> T waitForResult(ReporterRunnable<T> runnable)
      throws LogoException;

  void importWorld(java.io.Reader reader)
      throws java.io.IOException;

  void importWorld(String path)
      throws java.io.IOException;

  void importDrawing(String path)
      throws java.io.IOException;

  void clearDrawing();

  void exportDrawing(String path, String format)
      throws java.io.IOException;

  void exportView(String path, String format)
      throws java.io.IOException;

  java.awt.image.BufferedImage exportView();

  void exportInterface(String path)
      throws java.io.IOException;

  void exportWorld(String path)
      throws java.io.IOException;

  void exportWorld(java.io.PrintWriter writer)
      throws java.io.IOException;

  void exportOutput(String path)
      throws java.io.IOException;

  void exportPlot(String plotName, String path)
      throws java.io.IOException;

  void exportAllPlots(String path)
      throws java.io.IOException;

  void inspectAgent(org.nlogo.api.Agent agent, double radius);

  void inspectAgent(Class<? extends Agent> agentClass, org.nlogo.agent.Agent agent, double radius);

  java.awt.image.BufferedImage getAndCreateDrawing();

  HubNetInterface getHubNetManager();

  void waitForQueuedEvents()
      throws LogoException;

  void outputObject(Object object, Object owner,
                    boolean addNewline, boolean readable,
                    OutputDestination destination)
      throws LogoException;

  void clearOutput();

  void clearAll()
      throws LogoException;

  Procedure compileForRun(String source, Context context,
                          boolean reporter)
      throws CompilerException;

  String convertToNormal() throws java.io.IOException;

  String getModelPath();

  void setModelPath(String path);

  String getModelDir();

  String getModelFileName();

  FileManager fileManager();

  // kludgy this is Object, but we don't want to have a compile-time dependency
  // on org.nlogo.plot. - ST 2/12/08
  Object plotManager();

  void updatePlots(Context c);

  void setupPlots(Context c);

  String previewCommands();

  void tick(Context c, Instruction originalInstruction);

  void resetTicks(Context c);

  void clearTicks();

  String attachModelDir(String filePath)
      throws java.net.MalformedURLException;

  void evaluateCommands(JobOwner owner, String source)
      throws CompilerException;

  void evaluateCommands(JobOwner owner, String source, boolean waitForCompletion)
      throws CompilerException;

  void evaluateCommands(JobOwner owner, String source, org.nlogo.agent.Agent agent,
                        boolean waitForCompletion)
      throws CompilerException;

  void evaluateCommands(JobOwner owner, String source, org.nlogo.agent.AgentSet agents,
                        boolean waitForCompletion)
      throws CompilerException;

  Object evaluateReporter(JobOwner owner, String source)
      throws CompilerException;

  Object evaluateReporter(JobOwner owner, String source, org.nlogo.agent.Agent agent)
      throws CompilerException;

  Object evaluateReporter(JobOwner owner, String source, org.nlogo.agent.AgentSet agents)
      throws CompilerException;

  Procedure compileCommands(String source)
      throws CompilerException;

  Procedure compileCommands(String source, Class<? extends Agent> agentClass)
      throws CompilerException;

  Procedure compileReporter(String source)
      throws CompilerException;

  boolean runCompiledCommands(JobOwner owner, Procedure procedure);

  Object runCompiledReporter(JobOwner owner, Procedure procedure);

  void dispose() throws InterruptedException;

  double patchSize();

  void changeTopology(boolean wrapX, boolean wrapY);

  void open(String modelPath)
      throws java.io.IOException, CompilerException, LogoException;

  void openString(String modelContents)
      throws CompilerException, LogoException;

  void magicOpen(String name);

  void changeLanguage();

  void startLogging(String properties);

  void zipLogFiles(String filename);

  void deleteLogFiles();

  boolean getIsApplet();

  CompilerInterface compiler();

  boolean isHeadless();

  int behaviorSpaceRunNumber();

  void behaviorSpaceRunNumber(int n);

  // for now this only works in HeadlessWorkspace, returns null in GUIWorkspace.
  // this whole error handling stuff is a complete mess and needs to be redone
  // for the next release after 4.1 - ST 3/10.09
  LogoException lastLogoException();

  void clearLastLogoException();

  WeakHashMap<Job, WeakHashMap<Agent, WeakHashMap<Command, MutableLong>>> lastRunTimes(); // for _every

  WeakHashMap<Activation, Boolean> completedActivations(); // for _thunkdidfinish

  boolean profilingEnabled();

  Tracer profilingTracer();
}
