package bsearch.app;

import java.io.PrintStream;

import bsearch.representations.Chromosome;
import bsearch.evaluation.ResultListener;
import bsearch.evaluation.SearchManager;
import bsearch.nlogolink.ModelRunResult;

public class ConsoleProgressListener implements ResultListener {
    private String lastProgress;
    private int totalEvals;
    private PrintStream out;

    public ConsoleProgressListener(int totalEvals, PrintStream out)
    {
        this.totalEvals = totalEvals;
        this.out = out;
    }

    public void modelRunOccurred(SearchManager manager, Chromosome point,
            ModelRunResult result)
    {
        String progress = String.format("Search %s: %.0f%%\n", manager.getSearchIDNumber(),
                ((double) manager.getEvaluationCount()) / totalEvals * 100);
        if (!progress.equals(lastProgress))
        {
            out.print(progress);
            lastProgress = progress;
        }
    }


    public void fitnessComputed( SearchManager manager, Chromosome point, double fitness )
    {
    }

    public void newBestFound(SearchManager manager)
    {
    }

    public void searchStarting(SearchManager manager)
    {
        lastProgress = "N/A";
    }
    public void searchFinished(SearchManager manager)
    {
    }

    public void initListener(bsearch.space.SearchSpace space) {
    }

    public void allSearchesFinished() {
        out.printf("All searches completed.\n\n");
    }

    public void searchesAborted() {
        out.printf("Searches were aborted.\n\n");
    }

}
