package bsearch.evaluation;

import java.util.HashMap;

import bsearch.app.BehaviorSearchException;
import bsearch.representations.Chromosome;


public interface FitnessFunction
{

    /** Given a point in the search space, and a requested number of repetitions for that point, return a mapping which
     * contains all the points that would need to be evaluated in order to compute the fitness at the given point, and
     * how many additional repetitions (runs) are needed at each of those points.
     *
     */
    public abstract HashMap<Chromosome, Integer> getRunsNeeded(Chromosome point, int repetitionsRequested, ResultsArchive archive) throws BehaviorSearchException;

    /**
     * How many runs would be needed if caching were turned off
     * (or if caching is on, but none of the needed runs had been done previously)
     */
    public abstract int getMaximumRunsThatCouldBeNeeded(int repetitionsRequested);

    public abstract double evaluate(Chromosome point , ResultsArchive archive) throws BehaviorSearchException;

    /** @return a number representing how much better v2 is than v1 (0 if they are equal, negative if v1 is better) */
    public abstract double compare(double v1, double v2);

    /** @return true if v1 is a better fitness value than v2, and false if it is worse than or equal to v2 */
    public abstract boolean strictlyBetterThan(double v1, double v2);

    /**
     * @return a fitness value that is worse than anything that could actually be obtained by evaluating an individual.
     */
    public abstract double getWorstConceivableFitnessValue();

    /**
     * @return a fitness value that is better than anything that could actually be obtained by evaluating an individual.
     */
    public abstract double getBestConceivableFitnessValue();

    /** @return true if the fitness has reached a goal level, signaling that the search algorithm should terminate */
    public abstract boolean reachedStopGoalFitness(double fitness);


}
