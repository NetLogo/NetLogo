package bsearch.evaluation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import bsearch.app.SearchProtocol;
import bsearch.nlogolink.ModelRunResult;
import bsearch.representations.Chromosome;


public class StandardFitnessFunction implements FitnessFunction
{
    private final SearchProtocol protocol;

    public StandardFitnessFunction(SearchProtocol protocol)
    {
        this.protocol = protocol;
    }

    public HashMap<Chromosome, Integer> getRunsNeeded(Chromosome point, int repetitionsRequested, ResultsArchive archive)
    {
        LinkedHashMap<Chromosome, Integer> map = new LinkedHashMap<Chromosome,Integer>(1);
        map.put(point, StrictMath.max(0, repetitionsRequested - archive.getResultsCount(point)));
        return map;
    }
    public int getMaximumRunsThatCouldBeNeeded(int repetitionsRequested)
    {
        return repetitionsRequested;
    }


    public double evaluate(Chromosome point, ResultsArchive archive)
    {
        List<ModelRunResult> resultsSoFar = archive.getResults( point );
        LinkedList<Double> condensedResults = new LinkedList<Double>();
        for (ModelRunResult result: resultsSoFar)
        {
            List<Double> singleRunHistory = result.getPrimaryTimeSeries();
            double dResult = protocol.fitnessCollecting.collectFrom(singleRunHistory);

            condensedResults.add(dResult);
        }
        return protocol.fitnessCombineReplications.combine(condensedResults);
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

    //TODO: add support for stopGoalFitness TO SearchProtocol, the XML file, etc.

//  private boolean hasStopGoal;
//  private double stopGoalFitness;

    public boolean reachedStopGoalFitness(double fitness) {
        return false;  //TODO: Implement fitness goal stuff
        //throw new UnsupportedOperationException("Not implemented yet");
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
