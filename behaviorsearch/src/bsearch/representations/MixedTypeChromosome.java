package bsearch.representations;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.ParameterSpec;
import bsearch.space.SearchSpace;

/**
 * A simple Chromosomal representation with genotype is a list of values, one for each ParameterSpec in the search space.
 *   The chromosome is not made up solely of bits, but instead of various mixed types (numbers, objects), as defined by each
 *   ParameterSpec, and the way mutation is performed at a locus of the Chromosome is ParameterSpec specific.
 */
public class MixedTypeChromosome implements Chromosome
{
    private Object[] paramVals;
    private SearchSpace searchSpace;

    private MixedTypeChromosome( SearchSpace searchSpace , MersenneTwisterFast rng )
    {
        super();
        this.searchSpace = searchSpace;
        List<ParameterSpec> paramSpecs = searchSpace.getParamSpecs();
        paramVals = new Object[paramSpecs.size()];
        int i = 0;
        for (ParameterSpec p: paramSpecs)
        {
            paramVals[i++] = p.generateRandomValue( rng );
        }
    }

    private MixedTypeChromosome( SearchSpace searchSpace , LinkedHashMap<String,Object> paramSettings)
    {
        super();
        this.searchSpace = searchSpace;
        List<ParameterSpec> paramSpecs = searchSpace.getParamSpecs();
        if (paramSpecs.size() != paramSettings.size())
        {
            throw new IllegalStateException("# of parameter settings does not match search space parameter specifications.");
        }
        paramVals = new Object[paramSettings.size()];

        int i = 0;
        for (ParameterSpec p: paramSpecs)
        {
            //TODO: could be checking each of these paramSettings to make sure it's valid for the corresponding paramSpec?
            //  Or is it better not to?
            paramVals[i++] = paramSettings.get(p.getParameterName());
        }
    }

    private MixedTypeChromosome( Object[] paramVals, SearchSpace searchSpace )
    {
        super() ;
        this.paramVals = paramVals.clone();
        this.searchSpace = searchSpace ;
    }

    public LinkedHashMap<String,Object> getParamSettings()
    {
        LinkedHashMap<String,Object> paramSettings = new LinkedHashMap<String,Object>();
        int i = 0;
        for (ParameterSpec p: searchSpace.getParamSpecs())
        {
            paramSettings.put(p.getParameterName(),paramVals[i++]);
        }
        return paramSettings;
    }

    @Override
    public MixedTypeChromosome clone()
    {
        return new MixedTypeChromosome(paramVals, searchSpace);
    }

    public MixedTypeChromosome mutate(double mutRate, MersenneTwisterFast rng)
    {
        MixedTypeChromosome mc = this.clone();
        int i = 0;
        for (ParameterSpec ps: searchSpace.getParamSpecs())
        {
            if (rng.nextDouble() < mutRate)
            {
                //TODO: Consider exposing the mutationStrength parameter, instead of constant at 10% of parameter range.
                mc.paramVals[i] = ps.mutate( paramVals[i] , 0.1, rng ) ;
            }
            i++;
        }
        return mc;
    }

    public Chromosome[] crossoverWith(Chromosome other, MersenneTwisterFast rng)
    {
        if (! (other instanceof MixedTypeChromosome))
        {
            throw new IllegalStateException("Can't crossover different types of Chromosomes.");
        }
        MixedTypeChromosome parent0 = this;
        MixedTypeChromosome parent1 = (MixedTypeChromosome) other;
        MixedTypeChromosome[] children = new MixedTypeChromosome[2];
        children[0] = parent0.clone();
        children[1] = parent1.clone();
        int len = parent1.paramVals.length - 1 ;
        int splitPoint;
        if (len > 0)
        {
            splitPoint = 1 + rng.nextInt( len ) ;
        }
        else // we only have one parameter being searched, so crossover doesn't really happen.
        {
            splitPoint = 0;
        }

        for (int i = 0 ; i < len; i++)
        {
            if (i >= splitPoint)
            {
                children[0].paramVals[i] = parent1.paramVals[i];
                children[1].paramVals[i] = parent0.paramVals[i];
            }
        }
        return children;
    }

    public SearchSpace getSearchSpace()
    {
        return searchSpace;
    }

    /**
     * Redefine equality for our Chromosomes.
     * Two chromosomes are considered equal if they represent the same parameter values.
     *
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof MixedTypeChromosome)
        {
            MixedTypeChromosome dc = (MixedTypeChromosome) other;
            return Arrays.equals( paramVals, dc.paramVals);
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
        for (Object o: paramVals)
        {
            hashCode = hashCode * 2 + o.hashCode();
        }
        return hashCode;
    }

    public static class Factory implements ChromosomeFactory
    {
        public Chromosome createChromosome(SearchSpace searchSpace,
                MersenneTwisterFast rng) {
            return new MixedTypeChromosome(searchSpace, rng);
        }
        public Chromosome createChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings)
        {
            return new MixedTypeChromosome(searchSpace, paramSettings);
        }
        public String getHTMLHelpText() {
            return "<strong>MixedTypeChromosome</strong> This encoding most closely matches the way " +
                    "that one commonly thinks of the ABM parameters.  Each parameter is stored " +
                    "separately with its own data type (discrete numeric, continuous numeric, categorical, " +
                    "boolean, etc). Mutation applies to each parameter separately (e.g. continous " +
                    "parameters use Gaussian mutation, boolean parameters get flipped).";
        }
    }


}
