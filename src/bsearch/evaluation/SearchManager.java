package bsearch.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


import org.nlogo.api.MersenneTwisterFast;


import bsearch.app.BehaviorSearchException;
import bsearch.app.SearchProtocol;
import bsearch.nlogolink.BatchRunner;
import bsearch.nlogolink.ModelRunResult;
import bsearch.nlogolink.ModelRunner;
import bsearch.nlogolink.NetLogoLinkException;
import bsearch.nlogolink.ModelRunner.ModelRunnerException;
import bsearch.representations.Chromosome;


public class SearchManager
{
    private final BatchRunner runner;
    private final SearchProtocol protocol;

    private final FitnessFunction fitnessFunction;
    private int searchIDNumber;
    private double fitnessGoalLimit;
    private boolean stopAtFitnessGoal;

    private int evaluationCounter;
    private int requestedCounter;
    private int requestedCounterWhenLastEvaluated;
    private int auxilliaryEvaluationCounter;

    private Chromosome currentBest;
    private double currentBestFitness;
    private double currentBestFitnessCheckedEstimate;

    private ResultsArchive cache;

    private List<ResultListener> resultListeners = new ArrayList<ResultListener>();

    public SearchManager(int searchIDNumber, BatchRunner runner, SearchProtocol protocol,
            FitnessFunction fitnessFunction, boolean stopAtFitnessGoal, double fitnessGoalLimit )
    {
        this.searchIDNumber = searchIDNumber;
        this.runner = runner;
        this.protocol = protocol;
        this.fitnessFunction = fitnessFunction;
        this.evaluationCounter = 0 ;
        this.requestedCounter = 0;
        this.requestedCounterWhenLastEvaluated = 0;
        this.auxilliaryEvaluationCounter = 0;
        this.stopAtFitnessGoal = stopAtFitnessGoal;
        this.fitnessGoalLimit = fitnessGoalLimit;
        this.cache = new ResultsArchive(4096);

        setCurrentBest(null, fitnessFunction.getWorstConceivableFitnessValue());
    }

    public void addResultsListener(ResultListener listener)
    {
        resultListeners.add(listener);
    }

    /**
     * Note that this method *will* effect the state of the RNG, so you may wish to pass in a cloned copy of your RNG,
     *  or a brand new RNG, rather than an RNG that you are using for other purposes...
     * @param pointOfInterest
     * @param numReplicationsDesired
     * @param rng
     * @return
     * @throws ModelRunnerException
     * @throws BehaviorSearchException
     * @throws InterruptedException
     */
    private double computeFitnessWithoutSideEffects(Chromosome pointOfInterest, int numReplicationsDesired, MersenneTwisterFast rng)
        throws ModelRunnerException, NetLogoLinkException, InterruptedException, BehaviorSearchException
    {
        ResultsArchive tempCache = new ResultsArchive(numReplicationsDesired);
        HashMap<Chromosome, Integer> desiredRuns = fitnessFunction.getRunsNeeded(pointOfInterest, numReplicationsDesired, tempCache);

        List<Chromosome> evaluationQueue = new ArrayList<Chromosome>(numReplicationsDesired);
        for (Chromosome point : desiredRuns.keySet())
        {
            evaluationQueue.addAll(Collections.nCopies(desiredRuns.get(point), point ));
        }

        List<ModelRunner.RunSetup> setupList = new ArrayList<ModelRunner.RunSetup>(evaluationQueue.size());
        for (Chromosome point: evaluationQueue)
        {
            setupList.add(new ModelRunner.RunSetup(rng.nextInt(), point.getParamSettings()));
        }

        List<ModelRunResult> results = runner.doBatchRun(setupList);
        for (int i = 0; i < evaluationQueue.size(); i++)
        {
            tempCache.add(evaluationQueue.get(i), results.get(i), numReplicationsDesired);
        }
        auxilliaryEvaluationCounter += results.size();
        return fitnessFunction.evaluate(pointOfInterest, tempCache);
    }

    public double computeFitnessSingle(Chromosome point, int numReplicationsDesired, MersenneTwisterFast rng) throws ModelRunnerException, BehaviorSearchException, InterruptedException
    {
        Chromosome[] points = new Chromosome[] { point };
        return computeFitnessBatch(points, numReplicationsDesired, rng)[0];
    }
    public double[] computeFitnessBatch(Chromosome[] points, int numReplicationsDesired, MersenneTwisterFast rng) throws ModelRunnerException, BehaviorSearchException, InterruptedException
    {
        int[] repsDesired = new int[points.length];
        Arrays.fill(repsDesired, numReplicationsDesired);
        try {
            return computeFitnessBatch(points, repsDesired, rng);
        } catch (NetLogoLinkException ex) {
            ex.printStackTrace();
            throw new BehaviorSearchException(ex.getMessage(), ex.getCause());
        }
    }
    /**
     * Note: If protocol.caching is FALSE, then each call to this method will first clear
     * all the stored (cached) results before running the new batch of model runs.
     *
     * In addition to computing the fitness for this point, this method also checks if it's the best so far,
     * in which case it's stored as the current best.
     */
    public double[] computeFitnessBatch(Chromosome[] points, int[] numReplicationsDesired, MersenneTwisterFast rng)
        throws ModelRunnerException, NetLogoLinkException, InterruptedException, BehaviorSearchException
    {
        if (numReplicationsDesired.length != points.length)
        {
            throw new IllegalArgumentException("there must be some number of desired replications specified for *each* of the Chromosomes to be evaluated.");
        }

        if (!protocol.caching)
        {
            cache.clear();
        }

        //Notes:  Right now FitnessFunctions control how to allocate trials,
        // based on a hint from the search method (numReplicationsDesired...)
        // I think this will work, but there are some more use cases to consider:
        //  Adaptive # of replications (based on merit, or other?),
        //  derivative fitness (needs to evaluate nearby point as well, to approximate derivative),
        //  fitness approximation (needs to not evaluate points at all, in some cases?)

        HashMap<Chromosome, Integer> allDesiredRuns = new LinkedHashMap<Chromosome,Integer>();

        // as we add replications to be evaluated, keep track of how many are added for each point that we're
        // computing fitness for.
        int[] numReplicationsNeeded = new int[points.length];
        int prevRequested = 0;
        int requested = 0;

        for (int i = 0; i < points.length; i++)
        {
            requestedCounter += fitnessFunction.getMaximumRunsThatCouldBeNeeded(numReplicationsDesired[i]);
            HashMap<Chromosome, Integer> desiredRuns = fitnessFunction.getRunsNeeded(points[i], numReplicationsDesired[i], cache);
            for (Chromosome p: desiredRuns.keySet())
            {
                int oldReps = 0;
                if (allDesiredRuns.containsKey(p))
                {
                    oldReps = allDesiredRuns.get(p);
                }
                int newReps;

                if (protocol.caching)
                {
                    newReps = StrictMath.max(desiredRuns.get(p), oldReps);
                }
                else
                {
                    newReps = oldReps + desiredRuns.get(p);
                }

                allDesiredRuns.put(p, newReps);
                requested += (newReps - oldReps);
            }
            numReplicationsNeeded[i] = requested - prevRequested;
            prevRequested = requested;
        }

        List<Chromosome> evaluationQueue = new ArrayList<Chromosome>(requested);
        for (Chromosome point : allDesiredRuns.keySet())
        {
            evaluationQueue.addAll(Collections.nCopies(allDesiredRuns.get(point), point ));
        }

        List<ModelRunner.RunSetup> setupList = new ArrayList<ModelRunner.RunSetup>(evaluationQueue.size());
        for (Chromosome point: evaluationQueue)
        {
            //Note: We use a seed that is within the valid seed range for the PRNG.
            // users can recreate a specific run in NetLogo by running "RANDOM-SEED XXXX" and then running their model.
            int seed = rng.nextInt();
            setupList.add(new ModelRunner.RunSetup(seed, point.getParamSettings()));
        }

        // We create an auxilliaryRNG to use for "best-checking", so that the number of best-checking
        // replicates doesn't affect the search process at all.
        MersenneTwisterFast auxilliaryRNG = new MersenneTwisterFast(rng.clone().nextInt());

        // We save the value of the evaluationCounter here, so we can count up with it once while
        // adding the ModelRunResults to the cache, and then back it up, and do it again
        // when computing fitness.  I admit this seems a little odd, but it's simpler
        // than interspersing fitness computation in with adding results to the cache.
        // This issue only comes up because we are doing whole batches of model runs in parallel,
        // rather than doing serial fitness computation.  But for the sake of our ResultListeners,
        // we pretend that it happened in serial, so that they can get more fine-grained numbers
        // about when a new good solution was found.  Otherwise, a large-population GA which is
        // submitting large batch runs, will appear penalized in the data files because the
        // evaluationCounter would go up in large jumps before computing the fitness of any individual.
        int oldEvaluationCounter = evaluationCounter ;

        //TODO: Consider have the BatchRunner do a callback to the SearchManager, instead of just returning results?
        // Or perhaps less problematic, make it so that doBatchRun() is called first, but it exits rather than blocks,
        // and ResultArchive can poll for new results calling a getResult() method?
        //  ~Forrest 8/5/2009
        List<ModelRunResult> results = runner.doBatchRun(setupList);
        for (int i = 0; i < evaluationQueue.size(); i++)
        {
            evaluationCounter++;
            Chromosome point = evaluationQueue.get(i);

            // "protocol.fitnessSamplingReplications" is at least a good starting place, for
            // initializing the capacity of each list of model run results.
            // In common cases, it should be both perfectly memory and speed efficient...  ~Forrest (9/28/2009)
            cache.add(point, results.get(i), protocol.fitnessSamplingReplications);

            for (ResultListener listener : resultListeners)
            {
                listener.modelRunOccurred(this, point, results.get(i));
            }
        }
        evaluationQueue.clear();

        evaluationCounter = oldEvaluationCounter;
        double[] fitnesses = new double[points.length];
        for (int i = 0; i < points.length; i++)
        {
            double fitness = fitnessFunction.evaluate(points[i], cache);
            fitnesses[i] = fitness;
            evaluationCounter += numReplicationsNeeded[i];

            if (fitnessFunction.strictlyBetterThan(fitness, currentBestFitness))
            {
                setCurrentBest(points[i],fitness);
                if (protocol.useBestChecking())
                {
                    currentBestFitnessCheckedEstimate = computeFitnessWithoutSideEffects(points[i], protocol.bestCheckingNumReplications, auxilliaryRNG);
                }
                for (ResultListener listener : resultListeners)
                {
                    listener.newBestFound(this);
                }
            }
            if (numReplicationsNeeded[i] > 0) // i.e. we have new information
            {
                for (ResultListener listener : resultListeners)
                {
                    listener.fitnessComputed(this, points[i], fitness);
                }
            }
        }
        return fitnesses;
    }

    public List<ModelRunResult> getCachedResults( Chromosome point )
    {
        return cache.getResults(point);
    }

    /** @return the number of fitness evaluations that have been recorded  */
    public int getEvaluationCount()
    {
        return evaluationCounter;
    }
    public int getEvaluationsRequestedCount()
    {
        return requestedCounter;
    }
    /** @return the number of auxilliary fitness evaluations performed (notably, for "best checking") */
    public int getAuxilliaryEvaluationCount()
    {
        return auxilliaryEvaluationCounter;
    }
    /**
     * @return true if a very large evaluations requests have been made without any new evaluations actually taking place.
     * This will likely only happen if a search is completely stalled, and no longer exploring any new territory.
     */
    private boolean searchHasTotallyStalled()
    {
        if (protocol.fitnessSamplingReplications > 0)
        {
            int evaluationsPerPoint = fitnessFunction.getMaximumRunsThatCouldBeNeeded(protocol.fitnessSamplingReplications);
            return (requestedCounter - requestedCounterWhenLastEvaluated) > 10000000 * evaluationsPerPoint;
        }
        else
        {
            return (requestedCounter - requestedCounterWhenLastEvaluated) > 10000000;
        }
    }
    public boolean searchFinished()
    {
        return searchHasTotallyStalled() || evaluationCounter >= protocol.evaluationLimit
            || (stopAtFitnessGoal && fitnessFunction.reachedStopGoalFitness(fitnessGoalLimit));
    }
    public int getRemainingEvaluations()
    {
        return protocol.evaluationLimit - evaluationCounter;
    }
    public int getBatchRunnerNumThreads()
    {
        return runner.getNumThreads();
    }

    public int getSearchIDNumber()
    {
        return searchIDNumber;
    }


    public boolean fitnessStrictlyBetter(double f1, double f2)
    {
        return fitnessFunction.strictlyBetterThan(f1, f2);
    }
    public FitnessFunction getFitnessFunction()
    {
        return fitnessFunction;
    }


    /** In general we want to track the "current best" location in the space
     */
    private void setCurrentBest(Chromosome point, double fitness)
    {
        currentBest = point;
        currentBestFitness = fitness;
    }
    public Chromosome getCurrentBest()
    {
        return currentBest;
    }

    public double getCurrentBestFitness()
    {
        return currentBestFitness;
    }

    public double getCurrentBestFitnessCheckedEstimate()
    {
        return currentBestFitnessCheckedEstimate;
    }

    public double getBestFitnessCheckingReplications()
    {
        return protocol.bestCheckingNumReplications;
    }

    // TODO: This method probably doesn't belong here... refactor to a cleaner design?
    public Chromosome tournamentSelect( Chromosome[] population ,
            double[] fitness , int tournamentSize, MersenneTwisterFast rng)
    {
        //select several distinct random indices into the population array
        int[] randomIndices = new int[tournamentSize];
        int i = 0;
        while (i < randomIndices.length)
        {
            int randomIndex = rng.nextInt(population.length);
            for (int j = 0; j < i; j++)
            {
                if (randomIndices[j] == randomIndex)
                    continue;
            }
            randomIndices[i] = randomIndex;
            i++;
        }

        int bestIndex = -1;
        double bestFitness = fitnessFunction.getWorstConceivableFitnessValue() ;
        for (i = 0 ; i < randomIndices.length; i++)
        {
            double f = fitness[randomIndices[i]];
            if (fitnessFunction.strictlyBetterThan(f,bestFitness))
            {
                bestIndex = randomIndices[i];
                bestFitness = f;
            }
        }
        return population[bestIndex];
    }



}
