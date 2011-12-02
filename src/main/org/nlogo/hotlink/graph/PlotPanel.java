package org.nlogo.hotlink.graph;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import java.awt.*;
import java.util.ArrayList;

import org.nlogo.hotlink.main.MainWindow;
import org.nlogo.hotlink.view.ViewPanel;
import org.nlogo.app.PlotTab;

public class PlotPanel extends javax.swing.JPanel
						implements javax.swing.event.ChangeListener {
	
	private ArrayList<Plot> plots = new ArrayList<Plot>();
	//MainWindow mainWindow;
    PlotTab plotTab;
	int interval = 10;
    boolean showPiecewiseApproximation = true;
    boolean alreadyPopulated = false;

	public PlotPanel( PlotTab plotTab )
	{
		this.plotTab = plotTab;
	    setOpaque( true ) ;
	    setMinimumSize( new Dimension( 677 , 700 ) ) ;
	    //setMaximumSize( getPreferredSize() ) ;
	    setVisible(true);
	}
	
	public void addPlot( String name , int type , org.nlogo.plot.Plot netLogoPlot ) {
        //System.out.println("make new plot");
		Plot plot = new Plot( name , type , plotTab , this , netLogoPlot );

        //System.out.println("add the plot");
		add( plot );
        //System.out.println("add plot to list");
		plots.add( plot );
		//this.setPreferredSize(
		//		new java.awt.Dimension( this.getPreferredSize().width ,
		//				this.getPreferredSize().height + 200 ) );

        //System.out.println("done");
	}

    public ArrayList<Plot> getPlots() {
        return plots;
    }

    public Plot[] getPlotArray() {
        Plot[] plotArray = new Plot[plots.size()];
        int count = 0;
        for( Plot plot : plots ) {
            plotArray[count] = plot;
            count++;
        }
        return plotArray;
    }

	public Plot getPlot( int index ) {
		return plots.get( index );
	}

    public Plot getPlot( org.nlogo.plot.Plot netLogoPlot ) {
        for( Plot plot : plots ) {
            if( plot.getNetLogoPlot() == netLogoPlot ) {
                return plot;
            }
        }
        return null;
    }

    public Plot getPlot( String name ) {
        for( Plot plot : plots ) {
            if( plot.getName().compareTo( name ) == 0 ) {
                return plot;
            }
        }
        return null;
    }
	
	public void addNewTick( int graphIndex ) {
        //System.out.println("PlotPanel addNewTick graphIndex" + graphIndex);
		plots.get( graphIndex ).addNewTick();
        //System.out.println("end PlotPanel addNewTick graphIndex" + graphIndex);
	}
	
	public void addDataPoint( int run , int graphIndex , int series , double x , double y ) {
		plots.get( graphIndex ).addPoint( run, series , x , y );
	}
	
	public void addDataPoints( int run , int graphIndex , int series , double[][] data ) {
    	plots.get( graphIndex ).addBar( run, series , data );
	}

	public void addPen( int graphIndex , String name , Color color ) {
		plots.get( graphIndex ).addPen( name , color );
	}
	
	/*/
	 *  Handling
	/*/

	public void stateChanged(ChangeEvent e) {
		JSlider caller = (JSlider) e.getSource();
		setTick( caller.getValue() );
	}
	
	public void setTick( int tick ) {
		for( Plot graph : plotTab.getPlotPanel().getPlots() ) {
			graph.setTick( tick );
		}
	}
	
	public void renew() {
		//plots.clear();
		this.setVisible(true);
	}
	
	public void rePaint() {
		for( Plot graph : plotTab.getPlotPanel().getPlots() ) {
			graph.getPlot().rePaint();
		}
	}

	//public void setInterval(int interval) { this.interval = interval; }
	public int getInterval() { return plotTab.getDtDialog().interval; }
    public boolean getShowPiecewiseApproximation() { return showPiecewiseApproximation; }
    public void setShowPiecewiseApproximator(boolean selected) { showPiecewiseApproximation = selected; }

    public Frame getAppFrame() {
        return plotTab.getAppFrame();
    }

    public PlotTab getPlotTab() {
        return plotTab;
    }

    public ViewPanel getViewPanel() {
        return plotTab.getViewPanel();
    }

    public void setAlreadyPopulated() {
        alreadyPopulated = true;
    }

    public boolean alreadyPopulated() { return alreadyPopulated; }

    public ArrayList<String> plotNames() {
        ArrayList<String> names = new ArrayList<String>();
        for( Plot plot : plots ) {
            names.add(plot.getName());
        }

        return names;
    }
}