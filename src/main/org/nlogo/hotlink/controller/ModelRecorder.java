package org.nlogo.hotlink.controller;

import org.nlogo.hotlink.graph.HistoPlot;
import org.nlogo.hotlink.graph.LinePlot;
import org.nlogo.hotlink.graph.Plot;
import org.nlogo.hotlink.graph.PlotPanel;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.nlogo.hotlink.main.MainWindow;
import org.nlogo.app.App;
import org.nlogo.plot.PlotPen;
import org.nlogo.plot.PlotPoint;

/*****
 * For recording a run that's being executed in 
 * normal (with head) NetLogo that's being listened.
 * Waiting for tick events for this.
 * @author mwilkerson
 *
 */

public class ModelRecorder extends Thread implements TickListener {
    MainWindow mainWindow;
    String fileName;
    boolean isButtonThere = true;

    public ModelRecorder( MainWindow mainWindow ) {
        this.mainWindow = mainWindow;
    }

    @Override
    @SuppressWarnings("static-access")
    public void run() {

        java.io.File filesystemWorker = new java.io.File("tmp");

        if( filesystemWorker.mkdir() ) {
            System.out.println("Make the directory");
        } else {
            for( File file : filesystemWorker.listFiles() ) {
                file.delete();
            }
        }

        // so there's a logger to listen to
        System.setProperty("org.nlogo.loggingEnabled", "true");

        // start up the GUI
        App.main(new String[0]);


        // pop open user's desired model
        // TODO

        // start listening to the logger for ticks
        TickListeningAppender mySuperCoolAppender = new TickListeningAppender(this);
        org.nlogo.log.Logger.Globals().addAppender(mySuperCoolAppender);
	}
	/*
	public static void initializePlots( final PlotPanel graphPanel ) throws Exception, InvocationTargetException {
    	int plotCount = 0;
    	int penCount = 0;
    }*/
    	                                        /*
    	for( String plotName : App.app.workspace.plotManager().getPlotNames() ) { // for each plot
    		// check to see if it's histogram
    		boolean hist = false;
    		for( PlotPen pen : App.app.workspace.plotManager().getPlot( plotName ).pens() ) {
    			if( pen.mode() == PlotPen.BAR_MODE ) {
    				hist = true;
    			}
    		}
    		
    		if( hist ) {
    			graphPanel.addPlot( plotName , Plot.HISTOGRAM );
    		} else {
    			graphPanel.addPlot( plotName , Plot.LINE );
    		}
    		
        	penCount = 0;
        	for( PlotPen pen : App.app.workspace.plotManager().getPlot( plotName ).pens() ) {
        		//if( pen.points().size() > 0 ) {
		        	graphPanel.addPen( plotCount , pen.name() , penCount, new Color( pen.color() ) );
		        	penCount++;
        		//}
        	}
        	
        	plotCount++;
        }
        
    }

    private void populateGraphPanel() {
    	int plotCount = 0;

        //System.out.println("populate plots");

    	for( String plotName : App.app.workspace.plotManager().getPlotNames() ) { // for each plot
    		
        	int penCount = 0;
        	mainWindow.getGraphPanel().addNewTick(plotCount); // for histograms
        	
        	for( PlotPen pen : App.app.workspace.plotManager().getPlot( plotName ).pens() ) {
        		if( pen.points().size() > 0 ) {
	
	        		// if it's a time series
	    			if( pen.mode() == org.nlogo.plot.PlotPen.LINE_MODE ||
	    				pen.mode() == org.nlogo.plot.PlotPen.POINT_MODE ) {
	    				// TODO
		        		PlotPoint point = pen.points().get(pen.points().size() - 1);
		        		if( mainWindow.getGraphPanel().getPlot( plotCount ).getGraphType() == LinePlot.class ) {
		        			mainWindow.getGraphPanel().addDataPoint( plotCount , penCount , point.x(), point.y() );
		        		}
		        		// TODO: Handle noninterval time ticks
	    			} 
	    			
	    			if( pen.mode() == org.nlogo.plot.PlotPen.BAR_MODE ) {
	    				//get number of points for this pen
	    				for( org.nlogo.plot.PlotPoint point : pen.points() ) {
	        				if( mainWindow.getGraphPanel().getPlot( plotCount ).getGraphType() == HistoPlot.class ) {
	        					mainWindow.getGraphPanel().addDataPoint(plotCount, penCount, point.x(), point.y());
	        				}
	    				}
	    			}
        		}
    			penCount++;
        	}
        	plotCount++;
        }

        if( isButtonThere ) {
            mainWindow.remove( mainWindow.getLoadButton() );
            mainWindow.getViewPanel().refresh();
           // mainWindow.dtDialog.calibrateSpinner();
            mainWindow.add( mainWindow.getViewPanel() );
            mainWindow.add( mainWindow.getGraphPanel() );
            mainWindow.setVisible(true);
            isButtonThere = false;
        }
    }*/

    public void tick(double ticks) {
        //System.out.println( ticks );
        
        fileName = App.app().workspace().getModelFileName();

        if( ticks == 0.0 ) {
            try {
                //initializePlots(mainWindow.getGraphPanel());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
 
            //populateGraphPanel();
            //System.out.println("populate graph panel");
        } else {
            try {
                //App.app.workspace.exportView("tmp/" + Integer.toString( (int) ticks ), "png");
                //System.out.println("Adding an image");
                //populateGraphPanel();

            } catch ( Exception ex ) {
                System.out.println(ex.getMessage());
            }
        }
        try {
            Double myTicks = ticks;
            App.app().workspace().exportView("tmp/" + Integer.toString(myTicks.intValue()), "png");
            //mainWindow.getViewPanel().refresh();
        } catch (IOException ex) {
            System.out.println("blah!: " + ex.getMessage() );
        }
        //dtDialog.calibrateSpinner();
        //mainWindow.add( viewPanel );
        //mainWindow.add( graphPanel );
        //mainWindow.setVisible(true);

    }

    public String getFileName() {
        return this.fileName;
    }

}
