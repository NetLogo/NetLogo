package bsearch.evaluation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.app.BehaviorSearchException;
import bsearch.app.SearchProtocol;
import bsearch.nlogolink.ModelRunResult;
import bsearch.representations.Chromosome;
import bsearch.representations.DummyChromosome;


/**
 * This class is a work-in-progress.  Currently unused...
 */
public class DerivativeFitnessFunction implements FitnessFunction
{
    private final SearchProtocol protocol;
    private final String paramName;
    private final double deltaDistance;
    private final MersenneTwisterFast rng;

    private List<Chromosome> SPECIAL_MUTATE_pointsqueue = new LinkedList<Chromosome>();
    private List<Chromosome> SPECIAL_MUTATE_neighborsqueue = new LinkedList<Chromosome>();

    public DerivativeFitnessFunction(SearchProtocol protocol, MersenneTwisterFast rng) throws BehaviorSearchException
    {
        this.protocol = protocol;
        this.paramName = protocol.fitnessDerivativeParameter;
        this.deltaDistance = protocol.fitnessDerivativeDelta;
        this.rng = rng;
        if  (deltaDistance == 0)
        {
            throw new BehaviorSearchException("When taking the 'derivative' of the fitness function with respect to parameter X, the delta value (change in X) cannot be 0!");
        }

    }
    private Chromosome getPointDeltaNearby(Chromosome point) throws BehaviorSearchException
    {
        //Special case: if we set paramName to "@MUTATE@" then it chooses a neighboring point
        // in the search space by using mutation (with the mutation rate specified by deltaDistance)
        // from the current point.
        if (paramName.equals("@MUTATE@"))
        {
            double mutRate = deltaDistance;
            int failedMutationCounter = 0;
            final int MAX_MUTATION_ATTEMPTS = 1000000;
            Chromosome neighbor = point.mutate(mutRate, rng);
            while (neighbor.equals(point) && failedMutationCounter < MAX_MUTATION_ATTEMPTS )
            {
                neighbor = point.mutate(mutRate, rng);
            }
            if (failedMutationCounter == MAX_MUTATION_ATTEMPTS)
            {
                throw new BehaviorSearchException("An extremely large number of mutation attempts all resulted in no mutation - perhaps your mutation-rate is too low?");
            }
            SPECIAL_MUTATE_pointsqueue.add(point);
            SPECIAL_MUTATE_neighborsqueue.add(neighbor);
            return neighbor;
        }
        LinkedHashMap<String, Object> newParamSettings = new LinkedHashMap<String,Object>(point.getParamSettings());

        Object curVal = newParamSettings.get(paramName);
        if (curVal instanceof Number)
        {
            double val = ((Number) curVal).doubleValue();
            double newVal = (val - deltaDistance);
            newParamSettings.put(paramName, newVal);
        }
        else
        {
            throw new BehaviorSearchException("Derivative-based fitness measurements currently only work with numerical parameters!");
        }
        return new DummyChromosome(point.getSearchSpace(), newParamSettings);
    }

    public HashMap<Chromosome, Integer> getRunsNeeded(Chromosome point, int repetitionsRequested, ResultsArchive archive) throws BehaviorSearchException
    {
        LinkedHashMap<Chromosome, Integer> map = new LinkedHashMap<Chromosome,Integer>(1);
        map.put(point, StrictMath.max(0, repetitionsRequested - archive.getResultsCount(point)));
        Chromosome deltaComparePoint = getPointDeltaNearby(point);
        map.put(deltaComparePoint, StrictMath.max(0, repetitionsRequested - archive.getResultsCount(deltaComparePoint)));
        return map;
    }
    public int getMaximumRunsThatCouldBeNeeded(int repetitionsRequested)
    {
        return 2 * repetitionsRequested;
    }

    public double evaluate(Chromosome point, ResultsArchive archive) throws BehaviorSearchException
    {
        List<ModelRunResult> resultsSoFar = archive.getResults( point );

        LinkedList<Double> condensedResults = new LinkedList<Double>();
        for (ModelRunResult result: resultsSoFar)
        {
            List<Double> singleRunHistory = result.getPrimaryTimeSeries();
            double dResult = protocol.fitnessCollecting.collectFrom(singleRunHistory);

            condensedResults.add(dResult);
        }
        double pointVal = protocol.fitnessCombineReplications.combine(condensedResults);

        Chromosome neighbor;
        if (paramName.equals("@MUTATE@"))
        {
            int index = SPECIAL_MUTATE_pointsqueue.indexOf(point);
            neighbor = SPECIAL_MUTATE_neighborsqueue.get(index);
            SPECIAL_MUTATE_pointsqueue.remove(index);
            SPECIAL_MUTATE_neighborsqueue.remove(index);
        }
        else
        {
            neighbor = getPointDeltaNearby( point );
        }

        resultsSoFar = archive.getResults( neighbor );
        condensedResults = new LinkedList<Double>();
        for (ModelRunResult result: resultsSoFar)
        {
            List<Double> singleRunHistory = result.getPrimaryTimeSeries();
            double dResult = protocol.fitnessCollecting.collectFrom(singleRunHistory);

            condensedResults.add(dResult);
        }
        double deltaComparePointVal = protocol.fitnessCombineReplications.combine(condensedResults);

        double denominator = deltaDistance;

        // Special case, to see how much fitness varies between neighbors in the search space
        if (paramName.equals("@MUTATE@"))
        {
            denominator = 1;
        }

        if (protocol.fitnessDerivativeUseAbs)
        {
            return StrictMath.abs((pointVal - deltaComparePointVal) / denominator);
        }
        else
        {
            return (pointVal - deltaComparePointVal) / denominator;
        }
    }

    public double compare(double v1, double v2)
    {
        if (protocol.fitnessMinimized)
        {
            return v2 - v1 ;
        }
        else
        {
            return v1 - v2 ;
        }
    }
    public boolean strictlyBetterThan(double v1, double v2)
    {
        return compare(v1,v2) > 0.0;
    }

    public double getWorstConceivableFitnessValue() {
        return protocol.fitnessMinimized?Double.POSITIVE_INFINITY:Double.NEGATIVE_INFINITY;
    }
    public double getBestConceivableFitnessValue() {
        return protocol.fitnessMinimized?Double.NEGATIVE_INFINITY:Double.POSITIVE_INFINITY;
    }

    public boolean reachedStopGoalFitness(double fitness) {
        return false;  //TODO: not implemented (see comment in StandardFitnessFunction)
        /*if (!hasStopGoal)
        {
            return false;
        }
        else
        {
            if (protocol.fitnessMinimized)
            {
                return fitness <= stopGoalFitness;
            }
            else
            {
                return fitness >= stopGoalFitness;
            }
        }*/
    }


}
