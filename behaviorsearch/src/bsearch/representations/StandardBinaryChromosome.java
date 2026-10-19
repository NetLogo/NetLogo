package bsearch.representations;

import java.util.LinkedHashMap;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.SearchSpace;

/**
 * A Chromosomal representation that uses standard ordering of bits to encode integer (and discretized continuous) values
 */
public class StandardBinaryChromosome extends BinaryChromosome
{
    public StandardBinaryChromosome( SearchSpace searchSpace , MersenneTwisterFast rng )
    {
        super(searchSpace, rng);
    }
    public StandardBinaryChromosome( SearchSpace searchSpace , LinkedHashMap<String,Object> paramSettings )
    {
        super(searchSpace, paramSettings);
    }
    public StandardBinaryChromosome( boolean[] bitstring, SearchSpace searchSpace )
    {
        super(bitstring, searchSpace);
    }

    @Override
    public StandardBinaryChromosome clone()
    {
        return new StandardBinaryChromosome(bitstring, searchSpace);
    }

    @Override
    public void binaryEncode(long binval, boolean[] bits, int offset, int len)
    {
        long bitMask = 1;
        for (int i = 0 ; i < len ; i++)
        {
            bits[offset + i] = ((binval & bitMask) != 0) ;
            bitMask = bitMask << 1;
        }
    }

    @Override
    public long binaryDecode(boolean[] bits, int offset, int len )
    {
        long binval = 0;
        for (int i = len - 1 ; i >= 0 ; i--)
        {
            binval <<= 1;
            if (bits[offset + i])
            {
                binval = binval + 1;
            }
        }
        return binval;
    }

    public static class Factory implements ChromosomeFactory
    {
        public Chromosome createChromosome(SearchSpace searchSpace,
                MersenneTwisterFast rng) {
            return new StandardBinaryChromosome(searchSpace, rng);

        }
        public Chromosome createChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings)
        {
            return new StandardBinaryChromosome(searchSpace, paramSettings);
        }
        public String getHTMLHelpText() {
            return "<strong>StandardBinaryChromosome</strong> In this encoding, every parameter is converted " +
                    "into a string of binary digits, and these sequences are concatenated together into one " +
                    "large bit array.  Mutation and crossover then occur on a per-bit basis.";
        }
    }
}
