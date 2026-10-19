package bsearch.representations;

import java.util.LinkedHashMap;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.space.SearchSpace;

public interface ChromosomeFactory {

    public abstract Chromosome createChromosome(SearchSpace searchSpace , MersenneTwisterFast rng);
    public abstract Chromosome createChromosome( SearchSpace searchSpace, LinkedHashMap<String,Object> paramSettings);
    public abstract String getHTMLHelpText();

}
