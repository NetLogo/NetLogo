package bsearch.nlogolink;



public class ModelRunnerTask implements java.util.concurrent.Callable<ModelRunResult> {
    ModelRunner.RunSetup runSetup;
    ModelRunner.Factory factory;

    public ModelRunnerTask(ModelRunner.Factory factory, ModelRunner.RunSetup runSetup) {
        super();
        this.factory = factory;
        this.runSetup = runSetup;
    }

    public ModelRunResult call() throws NetLogoLinkException, ModelRunner.ModelRunnerException
    {
        ModelRunner runner = factory.acquireModelRunner();
        ModelRunResult result = runner.doFullRun(runSetup);
        factory.releaseModelRunner(runner);
        return result;
    }
}
