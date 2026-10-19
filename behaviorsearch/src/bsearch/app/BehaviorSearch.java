package bsearch.app;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.nlogo.api.MersenneTwisterFast;
import org.xml.sax.SAXException;

import bsearch.algorithms.SearchMethod;
import bsearch.algorithms.SearchMethodLoader;
import bsearch.algorithms.SearchParameterException;
import bsearch.evaluation.DerivativeFitnessFunction;
import bsearch.evaluation.StandardFitnessFunction;
import bsearch.evaluation.FitnessFunction;
import bsearch.evaluation.ResultListener;
import bsearch.evaluation.SearchManager;
import bsearch.nlogolink.BatchRunner;
import bsearch.nlogolink.ModelRunner;
import bsearch.nlogolink.NetLogoLinkException;
import bsearch.nlogolink.Utils;
import bsearch.representations.ChromosomeFactory;
import bsearch.representations.ChromosomeTypeLoader;
import bsearch.space.ParameterSpec;
import bsearch.space.SearchSpace;
import bsearch.util.GeneralUtils;

/**
 * NOTES:
 *  TODO: Eventually handle setting world-dimensions? and random-seed? (prob. not needed)
 *
 */
public class BehaviorSearch {

    public static void runMultipleSearches(SearchProtocol protocol, int numSearches, int firstSearchNumber, String fnameStem, List<ResultListener> listeners, int numThreads, int firstRandomSeed) throws BehaviorSearchException, InterruptedException, SearchParameterException
    {
        SearchSpace space = new SearchSpace(protocol.paramSpecStrings, protocol.modelFile.endsWith(".nlogo3d") || protocol.modelFile.endsWith(".nlogox3d"));

        for (ResultListener listener : listeners) {
            listener.initListener(space);
        }

        for (int searchNumber = firstSearchNumber; searchNumber < numSearches + firstSearchNumber; searchNumber++)
        {
            MersenneTwisterFast rng = new MersenneTwisterFast(firstRandomSeed + (searchNumber - firstSearchNumber));
            BehaviorSearch.runProtocol(protocol, space, searchNumber, numThreads, rng, listeners) ;
        }

        for (ResultListener listener : listeners) {
            listener.allSearchesFinished();
        }


    }

    public static SearchManager runProtocol(SearchProtocol protocol, SearchSpace space, int searchIDNumber, int numEvaluationThreads,  MersenneTwisterFast rng, List<ResultListener> listeners) throws SearchParameterException, BehaviorSearchException,  InterruptedException
    {
        boolean measureEveryTick = !protocol.fitnessCollecting.equals(SearchProtocol.FITNESS_COLLECTING.AT_FINAL_STEP);
        ModelRunner.Factory mrunnerFactory = new ModelRunner.Factory(
                GeneralUtils.attemptResolvePathFromProtocolFolder(protocol.modelFile), measureEveryTick,
                protocol.modelStepLimit, protocol.modelSetupCommands, protocol.modelStepCommands,
                protocol.modelStopCondition, protocol.modelMetricReporter, protocol.modelMeasureIf);
        BatchRunner batchRunner = new BatchRunner(numEvaluationThreads, mrunnerFactory);

        SearchMethod searcher = SearchMethodLoader.createFromName(protocol.searchMethodType);

        //if the search method in the protocol is missing some parameters, fill them in with defaults
        HashMap<String, String> defaultParams = searcher.getSearchParams();
        for (String key : defaultParams.keySet())
        {
            if (!protocol.searchMethodParams.containsKey(key))
            {
                protocol.searchMethodParams.put(key, defaultParams.get(key));
            }
        }
        searcher.setSearchParams(protocol.searchMethodParams);

        FitnessFunction ffun;
        if (protocol.fitnessSamplingReplications == 0 && !searcher.supportsAdaptiveSampling())
        {
            throw new BehaviorSearchException("Error: " + searcher.getName() + " does not support adaptive fitness sampling!");
        }
        else
        {
            if (protocol.fitnessDerivativeParameter.length() > 0)
            {
                ffun = new DerivativeFitnessFunction(protocol,rng);
            }
            else {
                ffun = new StandardFitnessFunction(protocol) ;
            }
        }

        SearchManager archive = new SearchManager(searchIDNumber, batchRunner, protocol, ffun, false, 0.0);

        for (ResultListener listener: listeners)
        {
            archive.addResultsListener(listener);
        }


        ChromosomeFactory cFactory = ChromosomeTypeLoader.createFromName(protocol.chromosomeType);
        try {
            for (ResultListener listener : listeners) {
                listener.searchStarting(archive);
            }
            searcher.search( space , cFactory, protocol, archive, rng );
            for (ResultListener listener : listeners) {
                listener.searchFinished(archive);
            }
        }
        catch (NetLogoLinkException ex)
        {
            System.err.println("***" + ex.getMessage() + "***");
            throw new BehaviorSearchException(ex.getMessage());
        }
        finally {
            try {
                batchRunner.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return archive;
    }



    public static class RunOptions implements Cloneable {
        @Option(name="-p",aliases={"--protocol"},required=true,usage="file (.bsearch) from which to load the search experiment protocol.")
        public String protocolFilename;

        @Option(name="-o",aliases={"--output"},required=true,usage="output filename STEM: will create files named STEM.xxxx.csv")
        public String outputStem;

        @Option(name="-t",aliases={"--threads"},usage="number of simultaneous threads to run the search with (defaults to the number of processors available).")
        public int numThreads = Runtime.getRuntime().availableProcessors();

        @Option(name="-n",aliases={"--numsearches"},usage="number of times to repeat the search (default 1)")
        public int numSearches = 1;

        @Option(name="-f",aliases={"--firstsearchnum"},usage="searches will be numbered starting at this index (only affects search # column in the output data)")
        public int firstSearchNumber = 1;

        @Option(name="-r",aliases={"--randomseed"},usage="random seed to start the first search (additional searches will be seeded with following consecutive numbers)")
        // if none specified, choose a random integer to start with.
        public Integer randomSeed = (Integer) new MersenneTwisterFast().nextInt();

        @Option(name="-q",aliases={"--quiet"},usage="suppress printing progress to stdout")
        boolean quiet = false;

        //TODO: Decide if --shorten option is even worthwhile... sort of doubtful, since in the most general case, each
        // fitness evaluation may take an arbitrary number of model runs, meaning that whatever shorten factor you use,
        // you can't guarantee it'll work out nicely to get every Nth one - you might just get some of the Nth ones.
        // ~Forrest (12/23/2009)
        @Option(name="--shorten",usage="only print information to the .objectiveFunctionHistory.csv after every Nth model run.")
        int shortenOutputFactor = 1;

        @Option(name="-b", aliases={"--brief-output"},usage="shorthand flag for suppressing model-run-history and objective-function-history output, since these are the largest output files.")
        public  boolean briefOutput = false;

        @Option(name="--suppress-model-run-history",usage="don't create the .modelRunHistory.csv file")
        boolean suppressModelRunHistory = false;

        @Option(name="--suppress-objective-function-history",usage="don't create the .objectiveFunctionHistory.csv file")
        boolean suppressObjectiveFunctionHistory = false;

        @Option(name="--suppress-best-history",usage="don't create the .bestHistory.csv file")
        boolean suppressBestHistory = false;

        @Option(name="-v", aliases={"--version"},usage="print version number and exit")
        boolean printVersion = false;

    @Option(name="--override-parameter-spec",usage="override the parameter spec/range given in the .bsearch file, using usual syntax (e.g. [\"population\" 100 200 400] (NOTE: you may need to \\ escape the quotes in your shell)", multiValued=true)
        List<String> overrideParameters = new java.util.LinkedList<String>();

        //@Argument(usage="any number of arguments...")
        //List<String> arguments;

        @Override
        public RunOptions clone()
        {
            try {
                return (RunOptions) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();  // won't happen, since class implements Cloneable
            }
        }
    }

    public static void main(String[] args) {
        RunOptions runOptions = new RunOptions();
        CmdLineParser parser = new CmdLineParser(runOptions);

        try {
            parser.parseArgument(args);

        } catch( CmdLineException e ) {
            if (runOptions.printVersion)
            {
                System.out.println("BehaviorSearch v" + GeneralUtils.getVersionString());
                System.exit(0);
            }
            if (args.length == 0)
            {
                System.err.println("BehaviorSearch v" + GeneralUtils.getVersionString() + "\n");
            }
            else {
                System.err.println(e.getMessage());
            }
            String scriptName = "behaviorsearch-headless.sh";
            if (GeneralUtils.isOSWindows())
            {
                scriptName = "behaviorsearch-headless.bat";
            }
            System.err.println(scriptName + " [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: " + scriptName + " -p myexperiment.bsearch -o myoutput --threads 1 -n 5");
            System.exit(1);
        }
        try {
            runWithOptions(runOptions);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        finally {
            try {
                Utils.fullyShutDownNetLogoLink();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void runWithOptions(RunOptions runOptions) throws IOException, SAXException, BehaviorSearchException, InterruptedException, SearchParameterException
    {
        runWithOptions(runOptions,null,null);
    }
    /**
     * Runs BehaviorSearch with the specified options (command line options, or could be specified through the GUI)
     * @param runOptions - Options for running BehaviorSearch
     * @param protocol - SearchProtocol to use for the search.  If null, then load the protocol from runOptions.protocolFilename.
     * @param additionalListeners - a list of additional listeners that should receive events relating to the search progress.
     * @throws IOException
     * @throws SAXException
     * @throws BehaviorSearchException
     * @throws InterruptedException
     * @throws SearchParameterException
     */
    public static void runWithOptions(RunOptions runOptions, SearchProtocol protocol, List<ResultListener> additionalListeners) throws IOException, SAXException, BehaviorSearchException, InterruptedException, SearchParameterException
    {
        runOptions = (RunOptions) runOptions.clone(); // so we don't cause any side-effects to the parameter
        if (protocol == null)
        {
            runOptions.protocolFilename = GeneralUtils.attemptResolvePathFromStartupFolder(runOptions.protocolFilename);
            runOptions.outputStem = GeneralUtils.attemptResolvePathFromStartupFolder(runOptions.outputStem);
            protocol = SearchProtocol.loadFile(runOptions.protocolFilename);
        }
        GeneralUtils.updateProtocolFolder(runOptions.protocolFilename);

        // allow users to override parameter spec ranges from the command line...
        for (String override : runOptions.overrideParameters)
        {
            ParameterSpec newSpec = ParameterSpec.fromString(override, protocol.modelFile.endsWith(".nlogo3d") || protocol.modelFile.endsWith(".nlogox3d"));
            boolean foundReplacement = false;

            for (int i = 0; i < protocol.paramSpecStrings.size(); i++) {
                ParameterSpec spec = ParameterSpec.fromString(protocol.paramSpecStrings.get(i), protocol.modelFile.endsWith(".nlogo3d") || protocol.modelFile.endsWith(".nlogox3d"));
                if (spec.getParameterName().equals(newSpec.getParameterName())) {
                    protocol.paramSpecStrings.remove(i);
                    protocol.paramSpecStrings.add(i, override);
                    foundReplacement= true;
                }
            }
            if (!foundReplacement) {
                protocol.paramSpecStrings.add(override);
            }
        }

        if (runOptions.briefOutput)
        {
            runOptions.suppressModelRunHistory = true;
            runOptions.suppressObjectiveFunctionHistory = true;
        }

        ArrayList<ResultListener> listeners = new ArrayList<ResultListener>();
        listeners.add(new CSVLoggerListener(protocol, runOptions.outputStem, !runOptions.suppressModelRunHistory, !runOptions.suppressObjectiveFunctionHistory, !runOptions.suppressBestHistory, true, runOptions.shortenOutputFactor));

        if (!runOptions.quiet)
        {
            listeners.add(new ConsoleProgressListener(protocol.evaluationLimit, System.out));
        }
        if (additionalListeners != null)
        {
            listeners.addAll(additionalListeners);
        }

        BehaviorSearch.runMultipleSearches(protocol, runOptions.numSearches, runOptions.firstSearchNumber,
                runOptions.outputStem,
                listeners, runOptions.numThreads, runOptions.randomSeed );

    }
}
