package bsearch.evaluation;

import bsearch.nlogolink.ModelRunResult;
import bsearch.representations.Chromosome;
import bsearch.space.SearchSpace;

public interface ResultListener
{
    public void initListener(SearchSpace space);

    public void modelRunOccurred(SearchManager manager, Chromosome point, ModelRunResult result);

    public void fitnessComputed(SearchManager manager, Chromosome point, double fitness);

    public void newBestFound(SearchManager manager);

    public void searchStarting(SearchManager manager);
    public void searchFinished(SearchManager manager);


    public void allSearchesFinished();
    public void searchesAborted();


}
