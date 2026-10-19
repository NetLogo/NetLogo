package bsearch.nlogolink;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;

import org.nlogo.agent.Observer;
import org.nlogo.api.AgentException;
import org.nlogo.core.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.nvm.Procedure;
import org.nlogo.api.MersenneTwisterFast;
import org.nlogo.headless.HeadlessWorkspace;

public class ModelRunner {

    private HeadlessWorkspace workspace;

    private Procedure setupCommands;
    private Procedure stepCommands;
    private Procedure stopConditionReporter;
    private Procedure measureIfReporter = null;
    private final boolean recordEveryTick;
    private final int maxModelSteps;

    // Note: ModelRunner can collect multiple measures, for eventual support for extending to multi-objective optimization.
    private LinkedHashMap<String,Procedure> resultReporters = new LinkedHashMap<String,Procedure>();

    private boolean runIsDone;

    // Until setup() is run for the first time (and the random seed can be set appropriately),
    // we'll leave the JobOwners as null.
    private SimpleJobOwner mainJobOwner = null; // uses NetLogo world's mainRNG (and thus affects the state of the world)
    private SimpleJobOwner extraJobOwner = null; // uses a private RNG, and thus may not affect world state (unless the NetLogo code being run has side effects)

    private ModelRunner(String modelFileName, boolean recordEveryTick, int maxModelSteps)
        throws LogoException, IOException, CompilerException
    {
        workspace = Utils.createWorkspace(modelFileName.endsWith(".nlogo3d") || modelFileName.endsWith(".nlogox3d"));
        workspace.open(modelFileName, false);

        this.recordEveryTick = recordEveryTick;
        this.maxModelSteps = maxModelSteps;
    }

    public void setSetupCommands(String commands) throws CompilerException
    {
        setupCommands = workspace.compileCommands(commands);
    }
    public void setStepCommands(String commands) throws CompilerException
    {
        stepCommands = workspace.compileCommands(commands);
    }
    public void setStopConditionReporter(String reporter) throws CompilerException
    {
        if (reporter.trim().length() > 0)
        {
            stopConditionReporter = workspace.compileReporter(reporter);
        }
    }
    public void addResultReporter(String reporter) throws CompilerException
    {
        resultReporters.put(reporter, workspace.compileReporter(reporter));
    }
    public void setMeasureIfReporter(String reporter) throws CompilerException
    {
        if (reporter.trim().length() > 0)
        {
            measureIfReporter = workspace.compileReporter(reporter);
        }
    }
    public boolean checkStopCondition() throws NetLogoLinkException
    {
        if (extraJobOwner == null)
        {
            throw new IllegalStateException("ModelRunner.setup() must be called before running commands/reporters.");
        }
        if (stopConditionReporter != null)
        {
            Object obj = workspace.runCompiledReporter( extraJobOwner, stopConditionReporter);
            LogoException ex = workspace.lastLogoException();
            if (ex != null)
            {
                workspace.lastLogoException_$eq(null);
                throw new NetLogoLinkException(ex.toString());
            }
            if (!(obj instanceof Boolean))
            {
                throw new NetLogoLinkException("The stop condition reporter must report a TRUE/FALSE value.  Error occurred because it reported: " + obj);
            }
            return  (Boolean) obj;
        }
        return false;
    }

    public void setup(int seed, LinkedHashMap<String,Object> parameterSettings ) throws LogoException, AgentException, NetLogoLinkException
    {
        workspace.clearAll();
        for (String s: parameterSettings.keySet())
        {
            workspace.world().setObserverVariableByName(s, parameterSettings.get(s));
        }
        try {
            workspace.world().mainRNG().setSeed( seed );
            // For evaluating reporters (like measureIfReporter) we want to use a separate RNG
            // (which is seeded by a random number that depends on the random seed for this model run,
            //  so that the results are deterministic/repeatable, but will generate an independent
            // stream of random numbers that does not affect the main NetLogo world's RNG.)
            // This approach should allow the user to recreate a run by setting RANDOM-SEED XXX
            // and running SETUP followed by GO, without worrying about all of the additional conditions
            // and reporters affecting the state of the RNG and changing the outcome of the run...
            mainJobOwner = new SimpleJobOwner("BehaviorSearch ModelRunner Main", workspace.mainRNG(), org.nlogo.core.AgentKindJ.Observer());
            MersenneTwisterFast extraReporterRNG = new MersenneTwisterFast(workspace.mainRNG().clone().nextInt());
            extraJobOwner = new SimpleJobOwner("BehaviorSearch ModelRunner Extra", extraReporterRNG, org.nlogo.core.AgentKindJ.Observer());
        } catch (Exception ex) {ex.printStackTrace(); }

        if (setupCommands != null)
        {
            workspace.runCompiledCommands(mainJobOwner,setupCommands);
            LogoException ex = workspace.lastLogoException();
            if (ex != null)
            {
                workspace.lastLogoException_$eq(null);
                throw new NetLogoLinkException(ex.toString());
            }
        }

        runIsDone = checkStopCondition();
    }

    /** returns true if the run is finished
     * @throws NetLogoLinkException **/
    public boolean go() throws NetLogoLinkException
    {
        if (mainJobOwner == null)
        {
            throw new IllegalStateException("ModelRunner.setup() must be called before running commands/reporters.");
        }
        if (stepCommands != null)
        {
            workspace.runCompiledCommands(mainJobOwner, stepCommands );
            LogoException ex = workspace.lastLogoException();
            if (ex != null)
            {
                workspace.lastLogoException_$eq(null);
                throw new NetLogoLinkException(ex.toString());
            }
        }

        runIsDone = checkStopCondition();
        return runIsDone;
    }

    public LinkedHashMap<String,Object> measureResults() throws NetLogoLinkException
    {
        LinkedHashMap<String,Object> results = new LinkedHashMap<String,Object>();
        for (String key: resultReporters.keySet())
        {
            results.put(key, measureResultReporter(resultReporters.get(key)));
        }
        return results;
    }

    private Double measureResultReporter(Procedure reporter) throws NetLogoLinkException
    {
        if (extraJobOwner == null)
        {
            throw new IllegalStateException("ModelRunner.setup() must be called before running commands/reporters.");
        }
        Object obj = workspace.runCompiledReporter( extraJobOwner, reporter);
        LogoException ex = workspace.lastLogoException();
        if (ex != null)
        {
            workspace.lastLogoException_$eq(null);
            throw new NetLogoLinkException(ex.toString());
        }
        if (! (obj instanceof Double))
        {
            throw new NetLogoLinkException("Result reporters must return numeric results!  Invalid reported value was: " + obj );
        }
        return (Double) obj;
    }

    private boolean evaluateMeasureIfReporter() throws NetLogoLinkException
    {
        // If they left the field blank, we'll assume we should measure every time.
        if (measureIfReporter == null)
        {
            return true;
        }
        Object obj = workspace.runCompiledReporter(extraJobOwner, measureIfReporter).equals(Boolean.TRUE);
        LogoException ex = workspace.lastLogoException();
        if (ex != null)
        {
            workspace.lastLogoException_$eq(null);
            throw new NetLogoLinkException(ex.toString());
        }
        if (!(obj instanceof Boolean))
        {
            throw new NetLogoLinkException("The 'measure if' condition must report a TRUE/FALSE value.  Error occurred because it reported: " + obj);
        }
        return  (Boolean) obj;
    }
    private void conditionallyRecordResults(ModelRunResult results) throws NetLogoLinkException
    {
        if (extraJobOwner == null)
        {
            throw new IllegalStateException("ModelRunner.setup() must be called before running commands/reporters.");
        }

        if (evaluateMeasureIfReporter())
        {
            for (String key: resultReporters.keySet())
            {
                results.addResult(key, measureResultReporter(resultReporters.get(key)));
            }
        }
    }
    public ModelRunResult doFullRun(RunSetup runSetup) throws ModelRunnerException
    {
        try {
            ModelRunResult results = new ModelRunResult(runSetup.seed);
            setup(runSetup.seed, runSetup.parameterSettings);

            int steps;
            for (steps = 0; steps < maxModelSteps && !runIsDone; steps++)
            {
                if (recordEveryTick )
                {
                    conditionallyRecordResults(results);
                }
                go();
            }
            conditionallyRecordResults(results);
            if (results.isEmpty())
            {
                throw new NetLogoLinkException("No values were measured/collected during this model run! (Model was run for " + steps + " steps.)");
            }
            return results;
        }
        catch (Exception ex)  {
            throw new ModelRunnerException(runSetup, ex);
        }
    }

    public boolean isRunDone()
    {
        return runIsDone;
    }

    public void dispose() throws InterruptedException {
        workspace.dispose();
    }

    /** Just used for unit testing - preferable to use the ModelRunner.Factory paradigm */
    public static ModelRunner createModelRunnerForTesting (String s, boolean recordEveryTick, int maxModelSteps) throws LogoException, IOException, CompilerException
    {
        return new ModelRunner(s, recordEveryTick, maxModelSteps);
    }


    /** Note: This uses the "extra" random number generator, so it
     * won't affect the state of the main RNG for the run.
     * Still, be careful because some reporters could have side-effects in the model
     * (for instance, if the reporter creates some agents
     * (even if it kills them before its done, the "who" numbers of agents
     *  created after that will be different...)
     * @param reporter  the NetLogo reporter expression to run.
     * @return          the result of running the reporter
     * @throws CompilerException
     * @throws NetLogoLinkException
     */
    public Object report(String reporter) throws CompilerException, NetLogoLinkException
    {
        Procedure procReporter = workspace.compileReporter( reporter );
        Object obj = workspace.runCompiledReporter( extraJobOwner, procReporter );
        LogoException ex = workspace.lastLogoException();
        if (ex != null)
        {
            workspace.lastLogoException_$eq(null);
            throw new NetLogoLinkException(ex.toString());
        }
        return obj;
    }
    /**
     * Note: this method uses the main random-number generator, and thus
     * will affect the state of the RNG for future calls.
     * @param cmd - the NetLogo command to be run on the current headless workspace.
     * @throws CompilerException
     * @throws LogoException
     */
    public void command(String cmd) throws CompilerException, LogoException
    {
        workspace.command( cmd );
    }

    public static class ModelRunnerException extends NetLogoLinkException
    {
        private static final long serialVersionUID = 1L;
        private final RunSetup runSetup;

        public ModelRunnerException(RunSetup runSetup, Exception ex) {
            super("", ex);
            this.runSetup = runSetup;
        }

        public RunSetup getRunSetup()
        {
            return runSetup;
        }

        @Override
        public String getMessage()
        {
            return getCause().toString() +  "\n\nModel run configuration was: " + runSetup;
        }
    }

    public static class RunSetup {
        final int seed;
        final private LinkedHashMap<String,Object> parameterSettings;

        public RunSetup(int seed,
                LinkedHashMap<String, Object> parameterSettings) {
            super();
            this.seed = seed;
            this.parameterSettings = parameterSettings;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("{RANDOM-SEED: " + seed + ",\n  SETTINGS: {");
            for (String key : parameterSettings.keySet())
            {
                sb.append(key + "=" + org.nlogo.api.Dump.logoObject(parameterSettings.get(key), true, false) + ", ");
            }
            sb.append("}}");
            return sb.toString();
        }
    }

    public static class Factory {
        private List<ModelRunner> allModelRunners = Collections.synchronizedList(new java.util.LinkedList<ModelRunner>());
        private Queue<ModelRunner> unusedModelRunners = new java.util.concurrent.ConcurrentLinkedQueue<ModelRunner>();

        private final String modelFileName, setupCommands, stepCommands, stopCondition, metricReporter, measureIfReporter;
        private final boolean recordEveryTick;
        private final int maxModelSteps;

        public Factory(String modelFileName, boolean recordEveryTick, int maxModelSteps,
                String setupCommands, String stepCommands, String stopCondition, String metricReporter,
                String measureIfReporter)
        {
            this.modelFileName = modelFileName;
            this.setupCommands = setupCommands;
            this.stepCommands = stepCommands;
            this.stopCondition = stopCondition;
            this.metricReporter = metricReporter;
            this.measureIfReporter = measureIfReporter;
            this.recordEveryTick = recordEveryTick;
            this.maxModelSteps = maxModelSteps;

        }
        /** Either creates a new one, or recycles a ModelRunner that has been released again into the pool */
        public ModelRunner acquireModelRunner() throws NetLogoLinkException
        {
            ModelRunner runner = unusedModelRunners.poll();
            if (runner != null)
            {
                return runner;
            }
            else
            {
                return newModelRunner();
            }
        }
        public void releaseModelRunner(ModelRunner runner)
        {
            unusedModelRunners.add(runner);
        }

        private ModelRunner newModelRunner() throws NetLogoLinkException
        {
            ModelRunner runner;
            try {
                runner = new ModelRunner(modelFileName, recordEveryTick, maxModelSteps);
            } catch (LogoException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error opening NetLogo model.  NetLogo sent back this error message: \"" + e.getMessage() + "\"");
            } catch (IOException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("I/O error when loading NetLogo model ( " + modelFileName + " ).  Error message: \"" + e.getMessage() + "\"");
            } catch (CompilerException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error compiling NetLogo model ( " + modelFileName + " ).  NetLogo's error message: \"" + e.getMessage() + "\"");
            }

            try {
                runner.setSetupCommands(setupCommands);
            } catch (CompilerException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error compiling the model's setup commands : " +setupCommands.toUpperCase() + " \n  NetLogo's error message: \"" + e.getMessage() + "\"");
            }

            try {
                runner.setStepCommands(stepCommands);
            } catch (CompilerException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error compiling the model's step commands : " +stepCommands.toUpperCase() + " \n  NetLogo's error message: \"" + e.getMessage() + "\"");
            }
            try {
                runner.setStopConditionReporter(stopCondition );
            } catch (CompilerException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error compiling the model's stop condition: " +stopCondition.toUpperCase() + " \n  NetLogo's error message: \"" + e.getMessage() + "\"");
            }
            try {
                runner.setMeasureIfReporter(measureIfReporter);
            } catch (CompilerException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error compiling the model's measure-if condition: " +measureIfReporter.toUpperCase() + " \n  NetLogo's error message: \"" + e.getMessage() + "\"");
            }
            try {
                runner.addResultReporter(metricReporter);
            } catch (CompilerException e) {
                e.printStackTrace();
                throw new NetLogoLinkException("Error compiling the model's fitness metric: " +metricReporter.toUpperCase() + " \n  NetLogo's error message: \"" + e.getMessage() + "\"");
            }

            allModelRunners.add(runner);

            return runner;
        }

        /** This method should only be called after you are done using this Factory, and all the ModelRunners created by it.
         * It disposes the ModelRunners (and their corresponding NetLogo workspaces), and attempting to use them after this
         * will result in errors.
         * */
        public void disposeAllRunners() throws InterruptedException {
            synchronized(allModelRunners)
            {
                for (ModelRunner runner : allModelRunners) {
                    runner.dispose();
                }
                allModelRunners.clear();
            }
        }

    }

}
