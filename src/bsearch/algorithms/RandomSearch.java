package bsearch.algorithms;

import java.util.HashMap;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.app.BehaviorSearchException;
import bsearch.app.SearchProtocol;
import bsearch.evaluation.SearchManager;
import bsearch.nlogolink.NetLogoLinkException;
import bsearch.representations.Chromosome;
import bsearch.representations.ChromosomeFactory;
import bsearch.space.SearchSpace;

public class RandomSearch extends AbstractSearchMethod {

    public RandomSearch()
    {
    }

    public String getName() {
        return "RandomSearch";
    }
    public String getDescription() {
        return "A baseline search algorithm that repeatedly tries random locations in the search space, and returns the best found.";
    }

    public void setSearchParams( HashMap<String , String> searchMethodParams ) throws SearchParameterException
    {
    }
    public HashMap<String , String> getSearchParams()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        return params;
    }
    public HashMap<String , String> getSearchParamsHelp()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        return params;
    }

    public void search( SearchSpace space , ChromosomeFactory cFactory, SearchProtocol protocol,
            SearchManager manager, MersenneTwisterFast rng ) throws BehaviorSearchException, NetLogoLinkException, InterruptedException
    {
        final int BATCH_SIZE = 16;  // processing model runs in batches allows us to take advantage of multi-threading/multi-processors
        int maxRunsForBatch = BATCH_SIZE * manager.getFitnessFunction().getMaximumRunsThatCouldBeNeeded(protocol.fitnessSamplingReplications);
        if (maxRunsForBatch == 0)  // assume some sort of adaptive sampling, so we don't really know
        {
            maxRunsForBatch = Integer.MAX_VALUE;  // turn off batching, to be safe.
        }

        while (!manager.searchFinished())
        {
            if (manager.getRemainingEvaluations() > maxRunsForBatch )
            {
                Chromosome[] points = new Chromosome[BATCH_SIZE];
                for (int i = 0; i < points.length; i++)
                {
                    points[i] = cFactory.createChromosome(space, rng);
                }

                // Note that the archive automatically tracks the best found so far, so all we have to do is evaluate...
                manager.computeFitnessBatch(points, protocol.fitnessSamplingReplications, rng);
            }
            else
            {
                Chromosome point = cFactory.createChromosome(space, rng);
                // Note that the archive automatically tracks the best found so far, so all we have to do is evaluate...
                manager.computeFitnessSingle(point, protocol.fitnessSamplingReplications, rng);
            }
        }
    }

}
