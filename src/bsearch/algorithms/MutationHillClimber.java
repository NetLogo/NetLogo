package bsearch.algorithms;

import java.util.HashMap;


public class MutationHillClimber extends SimulatedAnnealing {

    public MutationHillClimber()
    {
    }

    public String getName() {
        return "MutationHillClimber";
    }
    public String getDescription() {
        return "A random mutation hill climber starts at a random location in the search space, and repeatedly tries mutating its location and moving if it improves fitness.  Sometimes called a stochastic hill climber, and also very similar to a 1+1 evolutionary strategy";
    }

    public void setSearchParams( HashMap<String , String> searchMethodParams ) throws SearchParameterException
    {
        HashMap<String,String> params = new HashMap<String,String>(searchMethodParams);
        params.put("initial-temperature", "0.0");
        params.put("temperature-change-factor", "0.0");
        super.setSearchParams(params);
    }
    public HashMap<String , String> getSearchParams()
    {
        HashMap<String,String> params = super.getSearchParams();
        params.remove("initial-temperature");
        params.remove("temperature-change-factor");
        return params;
    }
    public HashMap<String , String> getSearchParamsHelp()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("mutation-rate", "controls how much mutation occurs when choosing a new location to try to climb uphill");
        params.put("restart-after-stall-count", "if it can't find an uphill location after X attempts, jump to a random location in the search space and start climbing again.");
        return params;
    }


}
