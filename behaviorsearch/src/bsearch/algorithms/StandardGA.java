package bsearch.algorithms;

import java.util.Arrays;
import java.util.HashMap;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.app.BehaviorSearchException;
import bsearch.app.SearchProtocol;
import bsearch.evaluation.SearchManager;
import bsearch.nlogolink.NetLogoLinkException;
import bsearch.representations.Chromosome;
import bsearch.representations.ChromosomeFactory;
import bsearch.space.SearchSpace;

public class StandardGA extends AbstractSearchMethod{

    private double mutationRate = 0.01;
    private double crossoverRate = 0.7;
    private int populationSize = 50;
    private int tournamentSize = 3;
    private String populationModel = "generational";

    public StandardGA()
    {
    }

    public String getName()
    {
        return "StandardGA";
    }
    public String getDescription()
    {
        return "A standard Genetic Algorithm.";
    }

    public void setSearchParams( HashMap<String , String> searchMethodParams ) throws SearchParameterException
    {
        mutationRate = validDoubleParam(searchMethodParams, "mutation-rate", 0.0, 1.0);
        crossoverRate = validDoubleParam(searchMethodParams, "crossover-rate", 0.0, 1.0);
        populationSize = validIntParam(searchMethodParams, "population-size", 2, 1000);
        tournamentSize = validIntParam(searchMethodParams, "tournament-size", 2, 10);
        populationModel = validChoiceParam(searchMethodParams, "population-model", Arrays.asList("generational","steady-state-replace-random","steady-state-replace-worst"));
    }
    public HashMap<String , String> getSearchParams()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("mutation-rate", Double.toString( mutationRate ));
        params.put("crossover-rate", Double.toString( crossoverRate ));
        params.put("population-size", Integer.toString( populationSize ));
        params.put("tournament-size", Integer.toString( tournamentSize ));
        params.put("population-model", populationModel);

        return params;
    }
    public HashMap<String , String> getSearchParamsHelp()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("mutation-rate", "likelihood of mutation occurring in each child");
        params.put("crossover-rate", "probability of using two parents to create a child (otherwise child is created asexually)");
        params.put("population-size", "the number of individuals in each generation");
        params.put("tournament-size", "for tournament selection, this is the number of individuals that compete to choose an individual that gets to reproduce."  );
        params.put("population-model", "'generational', 'steady-state-replace-random', or 'steady-state-replace-worst' -- generational means the whole population is replaced at once, whereas steady-state means that just a single individual is replaced by reproduction each iteration - either a randomly-chosen individual, or the current worst."  );
        return params;
    }


    public void search( SearchSpace space , ChromosomeFactory cFactory, SearchProtocol protocol ,
             SearchManager manager, MersenneTwisterFast rng )
        throws BehaviorSearchException, NetLogoLinkException, InterruptedException
    {
        Chromosome[] population = new Chromosome[populationSize];

        for (int i = 0; i < populationSize; i++)
        {
            population[i] = cFactory.createChromosome(space, rng);
        }

        double[] fitness = manager.computeFitnessBatch(population, protocol.fitnessSamplingReplications, rng);

        if (populationModel.equals("generational"))
        {
            Chromosome[] newpopulation = new Chromosome[populationSize];
            while ( !manager.searchFinished() )
            {
                int crossoverPairs = (int) (crossoverRate * populationSize / 2) ;
                int newPopIndex = 0;
                for (int i = 0; i < crossoverPairs; i++)
                {
                    Chromosome p1 = manager.tournamentSelect(population, fitness, tournamentSize, rng);
                    Chromosome p2 = manager.tournamentSelect(population, fitness, tournamentSize, rng);
                    Chromosome[] children = p1.crossoverWith( p2 , rng ) ;
                    newpopulation[newPopIndex++] = children[0];
                    newpopulation[newPopIndex++] = children[1];
                }
                while (newPopIndex < populationSize)
                {
                    newpopulation[newPopIndex++] = manager.tournamentSelect(population, fitness, tournamentSize, rng);
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
        else if (populationModel.startsWith("steady-state"))
        {
            while ( !manager.searchFinished() )
            {
                Chromosome child = manager.tournamentSelect(population, fitness, tournamentSize, rng);;
                if (rng.nextDouble() < crossoverRate)
                {
                    Chromosome p2 = manager.tournamentSelect(population, fitness, tournamentSize, rng);
                    child = child.crossoverWith( p2 , rng )[0] ;
                }
                child = child.mutate(mutationRate, rng);
                double childFitness = manager.computeFitnessSingle(child, protocol.fitnessSamplingReplications, rng);

                int replaceIndex;
                if (populationModel.equals("steady-state-replace-random"))
                {
                    replaceIndex = rng.nextInt(populationSize);
                }
                else
                {
                    double worstFitness = fitness[0];
                    replaceIndex = 0;
                    for (int i = 0; i < populationSize; i++)
                    {
                        if (manager.fitnessStrictlyBetter(worstFitness, fitness[i]))
                        {
                            replaceIndex = i;
                            worstFitness = fitness[i];
                        }
                    }
                }

                population[replaceIndex] = child;
                fitness[replaceIndex] = childFitness;
            }
        }
    }

}
