package bsearch.representations;

import java.util.LinkedHashMap;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.SearchSpace;

/**
 * A Chromosomal representation that uses Gray code for integers
 */
public class GrayBinaryChromosome extends BinaryChromosome
{
    public GrayBinaryChromosome( SearchSpace searchSpace , MersenneTwisterFast rng )
    {
        super(searchSpace, rng);
    }
    public GrayBinaryChromosome( SearchSpace searchSpace , LinkedHashMap<String,Object> paramSettings )
    {
        super(searchSpace, paramSettings);
    }
    public GrayBinaryChromosome( boolean[] bitstring, SearchSpace searchSpace )
    {
        super(bitstring, searchSpace);
    }

    @Override
    public GrayBinaryChromosome clone()
    {
        return new GrayBinaryChromosome(bitstring, searchSpace);
    }

    @Override
    public void binaryEncode(long binval, boolean[] graybits, int offset, int len)
    {
        // efficient shortcut for converting to Gray encoding
        long gray = binval ^ (binval >> 1);

        long bitMask = 1;
        for (int i = 0 ; i < len ; i++)
        {
            graybits[offset + i] = ((gray & bitMask) != 0) ;
            bitMask = bitMask << 1;
        }

//      Let B[n:0] be the input array of bits in the
//        usual binary representation, [0] being LSB
//        Let G[n:0] be the output array of bits in Gray code
//        G[n] = B[n]
//        for i = n-1 downto 0
//            G[i] = B[i+1] XOR B[i]

    }

    @Override
    public long binaryDecode(boolean[] graybits, int offset, int len )
    {
        long bitMask = 1L << (len - 1);
        long result = 0;
        boolean previousBit = false;
        if (graybits [offset + len - 1])
        {
            result |= bitMask;
            previousBit = true;
        }
        bitMask = bitMask >> 1;
        for (int i = len - 2; i >= 0; i--)
        {
            if (previousBit ^ graybits[ offset + i ] )
            {
                result = result | bitMask;
                previousBit = true;
            }
            else
            {
                previousBit = false;
            }
            bitMask = bitMask >> 1;
        }
        return result;
    }

    public static class Factory implements ChromosomeFactory
    {
        public Chromosome createChromosome(SearchSpace searchSpace,
                MersenneTwisterFast rng) {
            return new GrayBinaryChromosome(searchSpace, rng);
        }
        public Chromosome createChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings)
        {
            return new GrayBinaryChromosome(searchSpace, paramSettings);
        }
        public String getHTMLHelpText() {
            return "<strong>GrayBinaryChromosome</strong> Similar to StandardBinaryChromosome, " +
                    "except that numeric values are encoded to binary strings using a Gray code, " +
                    "instead of the standard 'high order' bit ordering. " +
                    "Gray codes have generally been found to give better performance for search representations, " +
                    "since numeric values that are close together are more likely to be fewer mutations away from each other.";
        }
    }

}
