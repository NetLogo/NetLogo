package bsearch.app;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import bsearch.representations.Chromosome;
import bsearch.space.ParameterSpec;
import bsearch.evaluation.ResultListener;
import bsearch.evaluation.SearchManager;
import bsearch.nlogolink.ModelRunResult;

public class CSVLoggerListener implements ResultListener {

    private SearchProtocol protocol;
    private PrintWriter modelHistoryOut = null;
    private PrintWriter fitnessOut = null;
    private int shortenOutputFactor = 1;
    private PrintWriter bestOut = null;
    private PrintWriter finalBestOut = null;
    private PrintWriter finalCheckedBestOut = null;


    /**
     * @param protocol - the SearchProtocol being used for the search.
     * @param fileNameStem - for saving multiple CSV files with a common starting stem
     * @param logAllModelRuns - log all evaluations that occur?
     * @param logAllFitnessEvals - log all fitness (objective) function evaluations that occur?
     * @param logBests - log the best points found?
     * @param logFinalBests - log the best points found?
     * @throws IOException
     * @throws BehaviorSearchException
     */
    public CSVLoggerListener(SearchProtocol protocol, String fileNameStem, boolean logAllModelRuns, boolean logAllFitnessEvals, boolean logBests, boolean logFinalBests, int shortenOutputFactor) throws IOException, BehaviorSearchException
    {
        this.protocol = protocol;
        this.shortenOutputFactor = shortenOutputFactor;
        if (logAllModelRuns)
        {
            modelHistoryOut = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileNameStem + ".modelRunHistory.csv")));
        }
        if (logAllFitnessEvals)
        {
            fitnessOut = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileNameStem + ".objectiveFunctionHistory.csv")));
        }
        if (logBests)
        {
            bestOut = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileNameStem + ".bestHistory.csv")));
        }
        if (logFinalBests)
        {
            finalBestOut = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileNameStem + ".finalBests.csv")));
            if (protocol.useBestChecking())
            {
                finalCheckedBestOut = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileNameStem + ".finalCheckedBests.csv")));
            }
        }

        // Also output a copy of the protocol that was used for this search.
        // It's the same format as the .bsearch files, but we use the .xml suffix
        // to differentiate it from the .bsearch file which was actually run.
        try {
            FileWriter fw = new java.io.FileWriter(fileNameStem + ".searchConfig.xml");
            protocol.save(fw);
            fw.close();
        } catch (java.io.IOException ex)
        {
            throw new BehaviorSearchException("File I/O error attempting to create or write to file: '" + fileNameStem + ".searchConfig.xml'", ex);
        }

    }

    public void initListener(bsearch.space.SearchSpace space)
    {
        if (modelHistoryOut != null)
        {
            List<String> headerList = new LinkedList<String>();
            headerList.add("search-number");
            headerList.add("evaluation");
            headerList.add("evals-requested-so-far");
            for (ParameterSpec p : space.getParamSpecs())
            {
                headerList.add(p.getParameterName() + "*");
            }
            headerList.add("random-seed");
            headerList.add("min-result");
            headerList.add("max-result");
            headerList.add("mean-result");
            headerList.add("stdev-result");
            headerList.add("final-step-result");
            modelHistoryOut.println(bsearch.nlogolink.CSVHelper.headerRow(headerList));
        }
        if (fitnessOut != null)
        {
            List<String> headerList = new LinkedList<String>();
            headerList.add("search-number");
            headerList.add("evaluation");
            for (ParameterSpec p : space.getParamSpecs())
            {
                headerList.add(p.getParameterName() + "*");
            }
            headerList.add("fitness");
            headerList.add("num-replicates");
            headerList.add("best-fitness-so-far");
            if (protocol.useBestChecking())
            {
                headerList.add("best-fitness-rechecked");
            }
            fitnessOut.println(bsearch.nlogolink.CSVHelper.headerRow(headerList));
        }

        List<String> headerList = new LinkedList<String>();
        headerList.add("search-number");
        headerList.add("evaluation");
        for (ParameterSpec p : space.getParamSpecs())
        {
            headerList.add(p.getParameterName() + "*");
        }
        headerList.add("num-replicates");
        headerList.add("best-fitness-so-far");
        if (protocol.useBestChecking())
        {
            headerList.add("recheck-replications");
            headerList.add("best-fitness-rechecked");
        }
//      headerList.add("trials-mean");
//      headerList.add("trials-variance");
//      headerList.add("trial-list-all");
        if (bestOut != null)
        {
            bestOut.println(bsearch.nlogolink.CSVHelper.headerRow(headerList));
        }
        if (finalBestOut != null)
        {
            finalBestOut.println(bsearch.nlogolink.CSVHelper.headerRow(headerList));
        }
        if (finalCheckedBestOut != null)
        {
            finalCheckedBestOut.println(bsearch.nlogolink.CSVHelper.headerRow(headerList));
            //TODO: should we use headerList here, since it will contain recheck-replications slot?
            // perhaps we should tailor it to this file more specifically?
        }

    }


    public void modelRunOccurred(SearchManager manager, Chromosome point, ModelRunResult result)
    {
        if (modelHistoryOut == null)
        {
            return;
        }
        //TODO: add in field for "[ticks]" ?
        List<Object> dataList = new LinkedList<Object>();
        dataList.add(manager.getSearchIDNumber());
        dataList.add(manager.getEvaluationCount());
        dataList.add(manager.getEvaluationsRequestedCount());
        for (ParameterSpec p : point.getSearchSpace().getParamSpecs())
        {
            dataList.add(point.getParamSettings().get(p.getParameterName()));
        }
        dataList.add(Long.toString(result.getRandomSeed()));
        LinkedList<Double> resultTimeSeries = result.getPrimaryTimeSeries() ;
        dataList.add(Collections.min(resultTimeSeries));
        dataList.add(Collections.max(resultTimeSeries));
        dataList.add(bsearch.util.Stats.mean(resultTimeSeries));
        dataList.add(bsearch.util.Stats.stdev(resultTimeSeries));
        dataList.add(resultTimeSeries.getLast());
        modelHistoryOut.println(bsearch.nlogolink.CSVHelper.dataRow(dataList));
    }

    public void fitnessComputed(SearchManager manager, Chromosome point, double fitness)
    {
        if (fitnessOut == null)
        {
            return;
        }
        if (manager.getEvaluationCount() % shortenOutputFactor != 0)
        {
            return;
        }
        List<Object> dataList = new LinkedList<Object>();
        dataList.add(manager.getSearchIDNumber());
        dataList.add(manager.getEvaluationCount());
        for (ParameterSpec p : point.getSearchSpace().getParamSpecs())
        {
            dataList.add(point.getParamSettings().get(p.getParameterName()));
        }
        dataList.add(fitness);
        List<ModelRunResult> allTrials = manager.getCachedResults(point);
        dataList.add(allTrials.size());
        dataList.add(manager.getCurrentBestFitness());
        if (protocol.useBestChecking())
        {
            dataList.add(manager.getCurrentBestFitnessCheckedEstimate());
        }
        fitnessOut.println(bsearch.nlogolink.CSVHelper.dataRow(dataList));
    }

    private List<Object> getRowDataForNewBest(SearchManager manager)
    {
        List<Object> dataList = new ArrayList<Object>();
        dataList.add(manager.getSearchIDNumber());
        dataList.add(manager.getEvaluationCount());
        Chromosome newBest = manager.getCurrentBest();
        for (ParameterSpec p : newBest.getSearchSpace().getParamSpecs())
        {
            dataList.add(newBest.getParamSettings().get(p.getParameterName()));
        }
        List<ModelRunResult> allTrials = manager.getCachedResults(newBest);
        dataList.add(allTrials.size());
        dataList.add(manager.getCurrentBestFitness());
        if (protocol.useBestChecking())
        {
            dataList.add(manager.getBestFitnessCheckingReplications());
            dataList.add(manager.getCurrentBestFitnessCheckedEstimate());
        }

        //TODO: put some more stats back in?
        /*
        dataList.add(bsearch.util.Stats.mean(allTrials));
        dataList.add(bsearch.util.Stats.variance(allTrials));
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Double d: allTrials)
        {
            sb.append(d);
            sb.append(" ");
        }
        sb.setCharAt(sb.length() - 1, ']');
        dataList.add(sb.toString());
        */
        return dataList;
    }

    private List<Object> lastBestRowData;
    private List<Object> checkedBestRowData;
    private double bestCheckedFitness;

    public void newBestFound(SearchManager manager)
    {
        lastBestRowData = getRowDataForNewBest(manager);
        if (protocol.useBestChecking())
        {
            double checkedFitness = manager.getCurrentBestFitnessCheckedEstimate();
            if (checkedBestRowData == null || manager.fitnessStrictlyBetter(checkedFitness,bestCheckedFitness))
            {
                checkedBestRowData = new ArrayList<Object>(lastBestRowData);
                bestCheckedFitness = checkedFitness;
            }
        }
        if (bestOut != null)
        {
            bestOut.println(bsearch.nlogolink.CSVHelper.dataRow(lastBestRowData));
        }
    }
    public void searchStarting(SearchManager manager)
    {
        lastBestRowData = null;
        checkedBestRowData = null;
    }
    public void searchFinished(SearchManager manager)
    {
        if (finalBestOut != null)
        {
            //TODO: Note that here we only update the evaluationCount -- other than that, it's the exact
            // same output as the final row of the .bestHistory file.  This may possibly be wrong, since
            // the number of replications may also have changed, if the point got evaluated more since then
            // (adaptive sampling?)...
            // The reason we use the stored data row, instead of asking for all fresh data from the SearchManager,
            // is because if we weren't caching, then the exact data that created the best will have been thrown away...
            // (in particular, we lost the # of replications, which is probably constant and unchanging...)
            lastBestRowData.set(1, manager.getEvaluationCount());
            finalBestOut.println(bsearch.nlogolink.CSVHelper.dataRow(lastBestRowData));
        }
        if (finalCheckedBestOut != null)
        {
            // See comment above...
            checkedBestRowData.set(1, manager.getEvaluationCount());
            finalCheckedBestOut.println(bsearch.nlogolink.CSVHelper.dataRow(checkedBestRowData));
        }

    }

    public void allSearchesFinished() {
        if (modelHistoryOut != null)
        {
            modelHistoryOut.flush();
            modelHistoryOut.close();
        }
        if (fitnessOut != null)
        {
            fitnessOut.flush();
            fitnessOut.close();
        }
        if (bestOut != null)
        {
            bestOut.flush();
            bestOut.close();
        }
        if (finalBestOut != null)
        {
            finalBestOut.flush();
            finalBestOut.close();
        }
        if (finalCheckedBestOut != null)
        {
            finalCheckedBestOut.flush();
            finalCheckedBestOut.close();
        }
    }

    public void searchesAborted() {
        // for now, handle aborted runs the same way as if it completed successfully.
        allSearchesFinished();
    }

}
