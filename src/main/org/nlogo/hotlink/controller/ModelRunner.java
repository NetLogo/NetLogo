package org.nlogo.hotlink.controller;

import org.nlogo.hotlink.dialogs.SetupWindowDialog;
import org.nlogo.hotlink.dialogs.InterfaceObjectPanel;

import org.nlogo.hotlink.graph.PlotPanel;  
import org.nlogo.hotlink.graph.Plot;
import org.nlogo.hotlink.graph.HistoPlot;
import org.nlogo.hotlink.graph.LinePlot;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoException;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.plot.*;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.io.*;

import javax.swing.JButton;

public class ModelRunner extends Thread {
	
	PlotPanel graphPanel = null;
	static HeadlessWorkspace workspace;
    JButton runButton;

    // Attempting to turn this into a thread
    String modelName;
    SetupWindowDialog setupWindowDialog;

    public ModelRunner( PlotPanel graphPanel , SetupWindowDialog setupWindowDialog ) {
        this.graphPanel = graphPanel;
        this.setupWindowDialog = setupWindowDialog;
    }

    public void setButton(JButton button) {
        this.runButton = button;
    }

    public void setModelFilename( String fileName ) {
        this.modelName = fileName;
    }

/*
    public List<BufferedImage> getImages() {
        while( this.isAlive() ) {
            //System.out.println("I'm running...");
        }
        System.out.println("... now I'm returning the images!");
        return imageList;
    }
	*/

    @Override
    public void run() {}
}
/*
        // if I've already run, I have images stored up. Dump those.

        java.io.File filesystemWorker = new java.io.File("tmp");

        if( filesystemWorker.mkdir() ) {
        } else {
            for( File file : filesystemWorker.listFiles() ) {
                file.delete();
            }
        }

        workspace = HeadlessWorkspace.newInstance();
        
        try {
            workspace.open( modelName );
            setupParameters();
            workspace.command("setup");

            // TODO: this might break if they don't populate the plots
            // before a go command? Check that out, yo.
            initializePlots( graphPanel );

            populateGraphPanel();
            for( int i = 0 ; i < setupWindowDialog.getTickCount(); i++ ) {
                // add a give-it-to
                workspace.command("go") ;
                // now we're trying file io instead.
                workspace.exportView("tmp/" + Integer.toString(i), "png");
            	populateGraphPanel();
            }
            workspace.dispose();
        } catch(Exception ex) {
            runButton.setText("Sorry, " + ex.getMessage() + ". Please try another model.");
        }

    }
    
    private void initializePlots( final PlotPanel graphPanel ) throws Exception, InvocationTargetException {
    	int plotCount = 0;
    	int penCount = 0;

    	for( String plotName : workspace.plotManager().getPlotNames() ) { // for each plot
    		// check to see if it's histogram
    		boolean hist = false;
    		for( PlotPen pen : workspace.plotManager().getPlot( plotName ). ) {
    			if( pen.mode() == PlotPen.BAR_MODE() ) {
    				hist = true;
    			}
    		}

            // api.exceptions.handle

    		if( hist ) {
    			graphPanel.addPlot( plotName , Plot.HISTOGRAM );
    		} else {
    			graphPanel.addPlot( plotName , Plot.LINE );
    		}
    		
        	penCount = 0;
        	for( PlotPen pen : workspace.plotManager().getPlot( plotName ).pens() ) {
		        	graphPanel.addPen( plotCount , pen.name() , penCount, new Color( pen.color() ) );
		        	penCount++;
        	}
        	
        	plotCount++;
        }
        
    }
    
    
    private void populateGraphPanel() {
    	int plotCount = 0;

    	for( String plotName : workspace.plotManager().getPlotNames() ) { // for each plot
    		
        	int penCount = 0;
        	graphPanel.addNewTick(plotCount); // for histograms
        	
        	for( PlotPen pen : workspace.plotManager().getPlot( plotName ).pens() ) {
        		if( pen.points().size() > 0 ) {
	
	        		// if it's a time series
	    			if( pen.mode() == org.nlogo.plot.PlotPen.LINE_MODE() ||
	    				pen.mode() == org.nlogo.plot.PlotPen.POINT_MODE() ) {
		        		PlotPoint point = pen.points().get(pen.points().size() - 1);
		        		if( graphPanel.getPlot( plotCount ).getGraphType() == LinePlot.class ) {
		        			graphPanel.addDataPoint( plotCount , penCount , point.x(), point.y() );
		        		}
		        		// TODO: Handle noninterval time ticks
	    			} 
	    			
	    			if( pen.mode() == org.nlogo.plot.PlotPen.BAR_MODE() ) {
	    				//get number of points for this pen
	    				for( org.nlogo.plot.PlotPoint point : pen.points() ) {
	        				if( graphPanel.getPlot( plotCount ).getGraphType() == HistoPlot.class ) {
	        					graphPanel.addDataPoint(plotCount, penCount, point.x(), point.y());
	        				}
	    				}
	    			}
        		}
    			penCount++;
        	}
        	plotCount++;
        }
    }

    private void setupParameters() {
        System.out.println(setupWindowDialog.getInterfaceObjects().size());
        for( InterfaceObjectPanel interfaceObjectPanel : setupWindowDialog.getInterfaceObjects() ) {
            System.out.println("got one!: " + interfaceObjectPanel.getName());
            try {
                workspace.world.setObserverVariableByName(
                        interfaceObjectPanel.getName(), interfaceObjectPanel.getValue() );
            } catch (AgentException ex) {
                System.out.println("AgentException! : " + ex.getMessage());
            } catch (LogoException ex) {
                System.out.println("LogoException! : " + ex.getMessage());
            }
        }
    }               */

