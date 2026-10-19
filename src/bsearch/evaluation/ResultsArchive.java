package bsearch.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bsearch.nlogolink.ModelRunResult;
import bsearch.representations.Chromosome;

public class ResultsArchive {
    // for storing the model run data in
    private HashMap<Chromosome, List<ModelRunResult>> cacheMap;

    public ResultsArchive(int initialCapacity)
    {
        cacheMap = new HashMap<Chromosome, List<ModelRunResult>>(initialCapacity);
    }

    public void clear()
    {
        cacheMap.clear();
    }

    public void add(Chromosome point, ModelRunResult result, int defaultNewListCapacity)
    {
        List<ModelRunResult> storedResults = cacheMap.get( point );
        if (storedResults == null)
        {
            storedResults = new ArrayList<ModelRunResult>(defaultNewListCapacity);
            storedResults.add(result);
            cacheMap.put( point , storedResults );
        }
        else
        {
            storedResults.add( result );
        }

    }

    /**
     * @param point
     * @return the list containing cached results for the given location in the search space,
     *  and an empty list if no results are known.
     */
    public List<ModelRunResult> getResults( Chromosome point )
    {
        List<ModelRunResult> rlist = cacheMap.get(point);
        if (rlist != null)
        {
            return rlist;
        }
        return new ArrayList<ModelRunResult>(0);
    }

    public int getResultsCount( Chromosome point )
    {
        List<ModelRunResult> rlist = cacheMap.get(point);
        if (rlist != null)
        {
            return rlist.size();
        }
        return 0;
    }


}
