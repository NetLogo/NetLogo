package bsearch.representations;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.ParameterSpec;
import bsearch.space.SearchSpace;

/**
 * A Chromosomal representation that uses Gray code for integers
 */
public abstract class BinaryChromosome implements Chromosome
{
    public static final int NUM_BITS_FOR_CONTINUOUS_PARAMETERS = 16; // discretized to 65,536 choices
    protected final boolean[] bitstring;
    protected final SearchSpace searchSpace;

    private static int numBitsRequired(long choiceCount)
    {
        if (choiceCount > 0)
        {
            return (int) StrictMath.ceil( StrictMath.log( choiceCount ) / StrictMath.log( 2 ));
        }
        else
        {
            return NUM_BITS_FOR_CONTINUOUS_PARAMETERS;
        }
    }
    public BinaryChromosome( SearchSpace searchSpace , MersenneTwisterFast rng )
    {
        super();
        this.searchSpace = searchSpace;
        List<ParameterSpec> paramSpecs = searchSpace.getParamSpecs();
        int bitstringLength = 0;
        for (ParameterSpec p: paramSpecs)
        {
            bitstringLength += numBitsRequired(p.choiceCount());
        }
        bitstring = new boolean[bitstringLength];

        int offset = 0;
        for (ParameterSpec p: paramSpecs)
        {
            int numbits = numBitsRequired(p.choiceCount());
            if (numbits > 0)
            {
                binaryEncode(rng.nextLong(1L << numbits), bitstring, offset, numbits);
                offset += numbits;
            }
        }
    }

    public BinaryChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings)
    {
        super();
        this.searchSpace = searchSpace;
        List<ParameterSpec> paramSpecs = searchSpace.getParamSpecs();
        if (paramSpecs.size() != paramSettings.size())
        {
            throw new IllegalStateException("# of parameter settings does not match search space parameter specifications.");
        }
        int bitstringLength = 0;
        for (ParameterSpec p: paramSpecs)
        {
            bitstringLength += numBitsRequired(p.choiceCount());
        }
        bitstring = new boolean[bitstringLength];

        int offset = 0;
        for (int i = 0; i < paramSpecs.size(); i++)
        {
            ParameterSpec p = paramSpecs.get(i);
            Object value = paramSettings.get(p.getParameterName());
            int numbits = numBitsRequired(p.choiceCount());
            long choice = p.getChoiceIndexFromValue(value, 1L << numbits);
            if (numbits > 0)
            {
                binaryEncode(choice, bitstring, offset, numbits);
                offset += numbits;
            }
        }
    }

    protected BinaryChromosome( boolean[] bitstring, SearchSpace searchSpace )
    {
        super() ;
        this.bitstring = bitstring.clone() ;
        this.searchSpace = searchSpace ;
    }

    public abstract void binaryEncode(long binval, boolean[] graybits, int offset, int len);

    public abstract long binaryDecode(boolean[] graybits, int offset, int len );

    static String toBinaryString(boolean[] bitstring)
    {
        String s = "";
        for (boolean b : bitstring)
        {
            s = (b?'1':'0') + s;
        }
        return s;
    }
    public String getBinaryString()
    {
        return toBinaryString(bitstring);
    }

    public LinkedHashMap<String,Object> getParamSettings()
    {
        LinkedHashMap<String,Object> paramSettings = new LinkedHashMap<String,Object>();
        int offset = 0;
        for (ParameterSpec p: searchSpace.getParamSpecs())
        {
            int numbits = numBitsRequired(p.choiceCount());
            if (numbits > 0)
            {
                paramSettings.put(p.getParameterName(), p.getValueFromChoice(binaryDecode(bitstring, offset, numbits), 1L << numbits));
                offset += numbits;
            }
            else // parameter has only one possible value, so just get it.
            {
                paramSettings.put(p.getParameterName(), p.getValueFromChoice(0,0));
            }
        }

        return paramSettings;
    }

    @Override
    public abstract BinaryChromosome clone();

    public Chromosome mutate(double mutRate, MersenneTwisterFast rng)
    {
        BinaryChromosome bc = clone();

        for (int i = 0; i < bc.bitstring.length; i++)
        {
            if (rng.nextDouble() < mutRate)
            {
                bc.bitstring[i] = ! bc.bitstring[i]; // flip a bit
            }
        }
        return bc;
    }

    public Chromosome[] crossoverWith(Chromosome other, MersenneTwisterFast rng)
    {
        if (! (other instanceof BinaryChromosome))
        {
            throw new IllegalStateException("Can't crossover different types of Chromosomes.");
        }
        BinaryChromosome parent0 = this;
        BinaryChromosome parent1 = (BinaryChromosome) other;
        BinaryChromosome[] children = new BinaryChromosome[2];
        children[0] = parent0.clone();
        children[1] = parent1.clone();
        int len = parent1.bitstring.length;
        int splitPoint = 1 + rng.nextInt( len ) ;
        for (int i = 0 ; i < len; i++)
        {
            if (i >= splitPoint)
            {
                children[0].bitstring[i] = parent1.bitstring[i];
                children[1].bitstring[i] = parent0.bitstring[i];
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
     * Two chromosomes are considered equal if they have the same genotype.
     *
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof BinaryChromosome)
        {
            BinaryChromosome gbc = (BinaryChromosome) other;
            return Arrays.equals( bitstring, gbc.bitstring);
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
        int hash = 0;
        for (boolean b : bitstring)
        {
            hash *= 2;
            hash += b ? 1 : 0;
        }
        return hash;
    }

}
