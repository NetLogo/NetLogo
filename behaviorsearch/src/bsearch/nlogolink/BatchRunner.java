package bsearch.nlogolink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import bsearch.nlogolink.ModelRunner.Factory;
import bsearch.nlogolink.ModelRunner.ModelRunnerException;

public class BatchRunner {

    private ModelRunner.Factory factory;
    ExecutorService pool;
    private final int numThreads;

    public BatchRunner(int numThreads, Factory factory) {
        this.factory = factory;
        this.numThreads = numThreads;
        pool = java.util.concurrent.Executors.newFixedThreadPool(numThreads);
    }
    public int getNumThreads()
    {
        return numThreads;
    }

    public List<ModelRunResult> doBatchRun(List<ModelRunner.RunSetup> setups) throws NetLogoLinkException, ModelRunnerException, InterruptedException
    {
        ArrayList<ModelRunnerTask> tasks = new ArrayList<ModelRunnerTask>();

        for (ModelRunner.RunSetup setup: setups)
        {
            tasks.add(new ModelRunnerTask(factory, setup));
        }
        List<Future<ModelRunResult>> futures = new ArrayList<Future<ModelRunResult>>(tasks.size());
        List<ModelRunResult> results = new ArrayList<ModelRunResult>(tasks.size());

        try {
            for (ModelRunnerTask task : tasks)
            {
                futures.add(pool.submit(task));
            }

            for (Future<ModelRunResult> future : futures)
            {
                results.add(future.get());
            }

            return results;
        }
        catch (ExecutionException ex) {
            if (ex.getCause() instanceof NetLogoLinkException)
            {
                throw (NetLogoLinkException) ex.getCause();
            }
            else if (ex.getCause() instanceof ModelRunnerException)
            {
                throw (ModelRunnerException) ex.getCause();
            }
            else
            {
                ex.printStackTrace();
                throw new NetLogoLinkException("Unexpected exception while attempting to run NetLogo", ex);
            }
        }
    }

    public void dispose() throws InterruptedException
    {
        pool.shutdownNow();
        factory.disposeAllRunners();
    }
}
