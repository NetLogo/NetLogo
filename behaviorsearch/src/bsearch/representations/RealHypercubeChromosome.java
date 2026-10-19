package bsearch.representations;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.DoubleContinuousSpec;
import bsearch.space.ParameterSpec;
import bsearch.space.SearchSpace;

/**
 * A Chromosomal representation where each parameter is represented by a real number on the interval [0,1),
 * regardless of whether the parameter only takes on a discrete set of values, or is in an entirely different range.
 */
public class RealHypercubeChromosome implements Chromosome
{
    private double[] dVals;
    private SearchSpace searchSpace;

    private RealHypercubeChromosome( SearchSpace searchSpace , MersenneTwisterFast rng )
    {
        super();
        this.searchSpace = searchSpace;
        List<ParameterSpec> paramSpecs = searchSpace.getParamSpecs();
        dVals = new double[paramSpecs.size()];
        for (int i = 0 ; i < dVals.length; i++)
        {
            dVals[i] = rng.nextDouble();
        }
    }
    private RealHypercubeChromosome( SearchSpace searchSpace , LinkedHashMap<String,Object> paramSettings)
    {
        super();
        this.searchSpace = searchSpace;
        List<ParameterSpec> paramSpecs = searchSpace.getParamSpecs();
        if (paramSpecs.size() != paramSettings.size())
        {
            throw new IllegalStateException("# of parameter settings does not match search space parameter specifications.");
        }
        dVals = new double[paramSpecs.size()];
        for (int i = 0 ; i < dVals.length; i++)
        {
            ParameterSpec paramSpec = paramSpecs.get(i);
            Object val = paramSettings.get(paramSpec.getParameterName());
            if (paramSpec instanceof DoubleContinuousSpec)
            {
                DoubleContinuousSpec dspec = (DoubleContinuousSpec) paramSpec;
                double paramVal = (Double) val;
                dVals[i] = (paramVal - dspec.getMin()) / (dspec.getMax() - dspec.getMin());
            }
            else
            {
                long choice = paramSpec.getChoiceIndexFromValue(val, paramSpec.choiceCount());
                dVals[i] = (double) choice / (double) paramSpec.choiceCount();
            }
        }
    }

    private RealHypercubeChromosome( double[] dVals, SearchSpace searchSpace )
    {
        super() ;
        this.dVals = dVals.clone();
        this.searchSpace = searchSpace ;
    }

    public LinkedHashMap<String,Object> getParamSettings()
    {
        LinkedHashMap<String,Object> paramSettings = new LinkedHashMap<String,Object>();
        int i = 0;
        for (ParameterSpec p: searchSpace.getParamSpecs())
        {
            double dVal = dVals[i++];
            if (p.choiceCount() < 0) // real-valued
            {
                if (p instanceof DoubleContinuousSpec)
                {
                    DoubleContinuousSpec dspec = (DoubleContinuousSpec) p;
                    double paramVal = dspec.enforceValidRange(dVal * (dspec.getMax() - dspec.getMin()) + dspec.getMin());
                    paramSettings.put(p.getParameterName(),paramVal);
                }
                else
                {
                    throw new IllegalStateException("Unknown real-valued parameter specification - this type of Chromosomal representation can't handle it.");
                }
            }
            else
            {
                long choice = (long) StrictMath.floor(dVal * p.choiceCount());
                Object paramVal = p.getValueFromChoice(choice, p.choiceCount());
                paramSettings.put(p.getParameterName(),paramVal);
            }
        }
        return paramSettings;
    }

    @Override
    public RealHypercubeChromosome clone()
    {
        return new RealHypercubeChromosome(dVals, searchSpace);
    }

    public RealHypercubeChromosome mutate(double mutRate, MersenneTwisterFast rng)
    {
        RealHypercubeChromosome rhc = this.clone();
        for (int i = 0 ; i < dVals.length; i++)
        {
            if (rng.nextDouble() < mutRate)
            {
                double newVal = (dVals[i] + 0.10 * rng.nextGaussian());
                if (newVal < 0.0 || newVal >= 1.0)
                {
                    newVal -= StrictMath.floor(newVal); //make it fall between 0.0 and 1.0
                }
                rhc.dVals[i] =  newVal;
            }
        }
        return rhc;
    }

    public Chromosome[] crossoverWith(Chromosome other, MersenneTwisterFast rng)
    {
        if (! (other instanceof RealHypercubeChromosome))
        {
            throw new IllegalStateException("Can't crossover different types of Chromosomes.");
        }
        RealHypercubeChromosome parent0 = this;
        RealHypercubeChromosome parent1 = (RealHypercubeChromosome) other;
        RealHypercubeChromosome[] children = new RealHypercubeChromosome[2];
        children[0] = parent0.clone();
        children[1] = parent1.clone();
        int len = parent1.dVals.length - 1 ;
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
                children[0].dVals[i] = parent1.dVals[i];
                children[1].dVals[i] = parent0.dVals[i];
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
        if (other instanceof RealHypercubeChromosome)
        {
            RealHypercubeChromosome dc = (RealHypercubeChromosome) other;
            return Arrays.equals( dVals, dc.dVals);
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
        for (Double d: dVals)
        {
            hashCode = hashCode * 2 + d.hashCode();
        }
        return hashCode;
    }

    public static class Factory implements ChromosomeFactory
    {
        public Chromosome createChromosome(SearchSpace searchSpace,
                MersenneTwisterFast rng) {
            return new RealHypercubeChromosome(searchSpace, rng);
        }
        public Chromosome createChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings)
        {
            return new RealHypercubeChromosome(searchSpace, paramSettings);
        }
        public String getHTMLHelpText() {
            return "<strong>RealHypercubeChromosome</strong> In this encoding, every parameter " +
                    "(numeric or not) is represented by a <I>real-valued</I> continuous variable.  " +
                    "This encoding exists mainly to facilitate the (future) use of algorithms " +
                    "that assume a continuous numeric space (such as Particle Swarm Optimization)," +
                    " and allow them to be applied even when some of the model parameters are not numeric.";
        }
    }

}
