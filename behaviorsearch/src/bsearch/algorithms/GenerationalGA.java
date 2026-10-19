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

@Deprecated
public class GenerationalGA extends AbstractSearchMethod{

    private double mutationRate = 0.01;
    private double crossoverRate = 0.7;
    private int populationSize = 50;

    public GenerationalGA()
    {
    }

    public String getName()
    {
        return "GenerationalGA";
    }
    public String getDescription()
    {
        return "A standard generational Genetic Algorithm wherein a new full replacement population is created in each generation. (Uses tournament selection with tournament size 3)";
    }

    public void setSearchParams( HashMap<String , String> searchMethodParams ) throws SearchParameterException
    {
        mutationRate = validDoubleParam(searchMethodParams, "mutation-rate", 0.0, 1.0);
        crossoverRate = validDoubleParam(searchMethodParams, "crossover-rate", 0.0, 1.0);
        populationSize = validIntParam(searchMethodParams, "population-size", 2, 1000);
    }
    public HashMap<String , String> getSearchParams()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("mutation-rate", Double.toString( mutationRate ));
        params.put("crossover-rate", Double.toString( crossoverRate ));
        params.put("population-size", Integer.toString( populationSize ));
        return params;
    }
    public HashMap<String , String> getSearchParamsHelp()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("mutation-rate", "likelihood of mutation occurring in each child");
        params.put("crossover-rate", "probability of using two parents to create a child (otherwise child is created asexually)");
        params.put("population-size", "the number of individuals in each generation");
        return params;
    }


    public void search( SearchSpace space , ChromosomeFactory cFactory, SearchProtocol protocol ,
             SearchManager manager, MersenneTwisterFast rng )
        throws BehaviorSearchException, NetLogoLinkException, InterruptedException
    {
        Chromosome[] population = new Chromosome[populationSize];
        Chromosome[] newpopulation = new Chromosome[populationSize];

        for (int i = 0; i < populationSize; i++)
        {
            population[i] = cFactory.createChromosome(space, rng);
        }

        double[] fitness = manager.computeFitnessBatch(population, protocol.fitnessSamplingReplications, rng);

        while ( !manager.searchFinished() )
        {
            int crossoverPairs = (int) (crossoverRate * populationSize / 2) ;
            int newPopIndex = 0;
            for (int i = 0; i < crossoverPairs; i++)
            {
                Chromosome p1 = manager.tournamentSelect(population, fitness, 3, rng);
                Chromosome p2 = manager.tournamentSelect(population, fitness, 3, rng);
                Chromosome[] children = p1.crossoverWith( p2 , rng ) ;
                newpopulation[newPopIndex++] = children[0];
                newpopulation[newPopIndex++] = children[1];
            }
            while (newPopIndex < populationSize)
            {
                newpopulation[newPopIndex++] = manager.tournamentSelect(population, fitness, 3, rng);
            }
            for (int i = 0; i < populationSize; i++)
            {
                newpopulation[i] = newpopulation[i].mutate( mutationRate , rng );
            }
            // swap in new population
            Chromosome[] temp = population;
            population = newpopulation;
            newpopulation = temp;

            fitness = manager.computeFitnessBatch(population, protocol.fitnessSamplingReplications, rng);
        }

    }


}
