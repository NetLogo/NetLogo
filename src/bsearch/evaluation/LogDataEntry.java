package bsearch.evaluation;

import bsearch.representations.Chromosome;

//TODO: Probably get rid of this class -- currently not using it.  ~Forrest (9/17/2009)
public class LogDataEntry
{
    public final int evaluationCount;
    public final Chromosome point;
    public final double result;
    public final Chromosome currentBest;
    public final int currentBestNumTrials;
    public final double currentBestFitnessMean;
    public final double currentBestFitnessVariance;

    public LogDataEntry(int evaluationCount, Chromosome point, double result,
            Chromosome currentBest, int currentBestNumTrials,
            double currentBestFitnessMean, double currentBestFitnessVariance)
    {
        super();
        this.evaluationCount = evaluationCount;
        this.point = point;
        this.result = result;
        this.currentBest = currentBest;
        this.currentBestNumTrials = currentBestNumTrials;
        this.currentBestFitnessMean = currentBestFitnessMean;
        this.currentBestFitnessVariance = currentBestFitnessVariance;
    }


}

