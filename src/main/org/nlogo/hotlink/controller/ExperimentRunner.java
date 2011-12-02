package org.nlogo.hotlink.controller;

import org.nlogo.api.CompilerException;
import org.nlogo.hotlink.view.View;
import org.nlogo.app.PlotTab;
import org.nlogo.workspace.AbstractWorkspace;
import java.lang.reflect.InvocationTargetException;
import org.nlogo.plot.PlotPen;
import org.nlogo.hotlink.graph.Plot;
import org.nlogo.plot.PlotPoint;
import org.nlogo.hotlink.graph.LinePlot;
import org.nlogo.hotlink.graph.HistoPlot;
import org.nlogo.hotlink.dialogs.ExperimentStatus;

import org.nlogo.hotlink.graph.PlotPanel;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Feb 4, 2010
 * Time: 9:37:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExperimentRunner extends Controller {
    ExperimentStatus experimentStatus = new ExperimentStatus(this);

    public ExperimentRunner( PlotTab plotTab , AbstractWorkspace workspace ) {
        super( plotTab , workspace );
    }

    public void run() {
        // file stuff : clear out any previous runs
        //String tmpDir = System.getProperty("java.io.tmpdir") + "dthl/";
        java.io.File filesystemWorker = new java.io.File("tmp");

        if( plotTab().getRunExperiment().ok() ) {

            plotTab().clearTmpDirectory();
            experimentStatus.setVisible(true);
            
            // TODO: Maybe have dropdowns of procedure names for setup and go eventually?
            for( int run = plotTab().getNumberOfRuns() ;
                    run <  plotTab().getRunExperiment().getRunCount() + plotTab().getNumberOfRuns() ; run++ ) {
                try {

                    workspace().evaluateCommands("setup");
                    if( run == 0 && !plotTab().getPlotPanel().alreadyPopulated() ) {  // only initialize plots on first run or else we'll have a ton
                        initializePlots();
                    }

                    // file stuff
                    filesystemWorker = new java.io.File("tmp/" + Integer.toString(run));
                    filesystemWorker.mkdir();

                    workspace().exportView("tmp/" + Integer.toString(run) + "/" + Integer.toString(0), "png");

                    for( int tick = 1 ; tick < plotTab().getNumberOfTicks() ; tick++ ) {
                        workspace().evaluateCommands("go");

                        // populate graphs
                        populateGraphPanel( run , plotTab().getNumberOfTicks() );
                        // save the image
                        workspace().exportView("tmp/" + Integer.toString(run) + "/" + Integer.toString(tick), "png");
                        experimentStatus.setTickProgress( 100 * tick/plotTab().getNumberOfTicks() , tick );
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                experimentStatus.setProgress( 100 * (run + 1)/plotTab().getRunExperiment().getRunCount() , run + 1 );
            }

            experimentStatus.setVisible(false);
            plotTab().getViewPanel().refresh();
            plotTab().addToRunCount( plotTab().getRunExperiment().getRunCount() );
            plotTab().populateContentPanel();

        }

    }

    
}
