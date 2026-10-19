package bsearch.representations;

import java.util.LinkedHashMap;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.SearchSpace;

/**
 * A dummy Chromosome, it just acts as a container for a set of parameter-settings.
 * Evolution algorithms should not use this Chromosome type for evolution --
 * it's just a convenient mechanism for representing a point in the search space
 * (or potentially outside the bounds/resolution of the search space)
 * so it can be evaluated using the existing BatchModel running infrastructure.
 */
public class DummyChromosome implements Chromosome
{
    SearchSpace searchSpace;
    LinkedHashMap<String,Object> paramSettings;

    public DummyChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings)
    {
        this.searchSpace = searchSpace;
        this.paramSettings = paramSettings;
    }

    public LinkedHashMap<String,Object> getParamSettings()
    {
        return paramSettings;
    }

    @Override
    public DummyChromosome clone()
    {
        return new DummyChromosome(searchSpace,new LinkedHashMap<String,Object>(paramSettings));
    }

    public DummyChromosome mutate(double mutRate, MersenneTwisterFast rng)
    {
        throw new IllegalStateException("Can't evolve DummyChromosome!");
    }

    public Chromosome[] crossoverWith(Chromosome other, MersenneTwisterFast rng)
    {
        throw new IllegalStateException("Can't evolve DummyChromosome!");
    }

    /** Note: Depending on how the DummyChromosome was constructed, it may or may not
     * represent a point in this search space.
     */
    public SearchSpace getSearchSpace()
    {
        return searchSpace;
    }


    /**
     * Redefine equality for DummyChromosomes.
     * Two chromosomes are considered equal if they represent the same parameter values.
     */
    public boolean equals(Object other)
    {
        if (other instanceof DummyChromosome)
        {
            DummyChromosome dc = (DummyChromosome) other;
            return paramSettings.equals(dc.paramSettings);
        }
        return false;
    }
    /**
     * Reflects the redefined equals(Object) method, and it
     * should provide more efficient hashing for Chromosome keys in hashmaps.
     */
    @Override
    public int hashCode()
    {
        int hashCode = 0;

        for (Object o: paramSettings.values())
        {
            hashCode = hashCode + o.hashCode();
        }
        return hashCode;
    }
}
