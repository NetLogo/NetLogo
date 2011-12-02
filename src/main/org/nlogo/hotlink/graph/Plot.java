package org.nlogo.hotlink.graph;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

import org.nlogo.hotlink.main.MainWindow;
import org.nlogo.app.PlotTab;

import org.jdesktop.swingx.JXCollapsiblePane;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;

public class Plot extends javax.swing.JPanel
	implements javax.swing.event.ChangeListener {
	
	private PlotWindow plotWindow;
	private PenCollection penCollection = new PenCollection();
	private JXCollapsiblePane innerPanel = new JXCollapsiblePane();
    org.nlogo.plot.Plot netLogoPlot;
    JLabel nameLabel = new JLabel();
	
	public final static int
		LINE = 0,
		HISTOGRAM = 1;
	
	public Plot(String name, int type, PlotTab plotTab , PlotPanel plotPanel , org.nlogo.plot.Plot netLogoPlot ) {
		this.setName(name);
        this.netLogoPlot = netLogoPlot;
        System.out.println("I'm in HotLink");

		if( type == LINE ) {
			plotWindow = new LinePlot( name , plotTab , penCollection , plotPanel , this , netLogoPlot );
            //System.out.println("end plotwindow");
		} else {
            //System.out.println("new histo");
			plotWindow = new HistoPlot( name , plotTab , penCollection , plotPanel , this , netLogoPlot );
		}

        //System.out.println("add stuff");
	    
	    innerPanel.add( plotWindow );
	    innerPanel.add( penCollection );
	    FlowLayout innerLayout = new FlowLayout();
	    innerPanel.setLayout( innerLayout );
	    
	    javax.swing.plaf.basic.BasicArrowButton toggle =
	    	new javax.swing.plaf.basic.BasicArrowButton( 
	    			javax.swing.plaf.basic.BasicArrowButton.EAST );
	    toggle.setAction(innerPanel.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
	    
	    javax.swing.JPanel togglePanel = new javax.swing.JPanel();
	    togglePanel.add(toggle);
	    togglePanel.add(nameLabel);
	    FlowLayout toggleLayout = new FlowLayout(FlowLayout.LEADING);
	    togglePanel.setLayout(toggleLayout);
	    
	    javax.swing.BoxLayout thisLayout = 
	    	new javax.swing.BoxLayout( this , javax.swing.BoxLayout.Y_AXIS );
	    setLayout( thisLayout );
	    
	    add(togglePanel);
	    add(innerPanel);
        //this.setBorder( new LineBorder( java.awt.Color.red ) );
	}
	
	public void addNewTick() {
		plotWindow.addNewTick();
	}


	public void addPoint( int run , int series , double x , double y ) {
		plotWindow.addDataPoint( run , series , x , y );
	}
	
	public void addBar( int run , int series , double[][] data ) {
		plotWindow.addDataPoint( run , series , data );
	}

	
	public void addPen( String penName , Color color ) {
		penCollection.addPen( penName, plotWindow , color );
	}

	public void stateChanged(ChangeEvent e) {
		plotWindow.stateChanged(e);
	}
	
	public void setTick( int tick ) {
		plotWindow.setTick( tick );
	}
	
	public PenCollection getCollection() {
		return penCollection;
	}
	
	public Class getGraphType() {
		return plotWindow.getClass();
	}
	
	public PlotWindow getPlot() {
		return plotWindow;
	}

    public void setName( String name ) {
        super.setName(name);
        nameLabel.setText(name);
    }

    public org.nlogo.plot.Plot getNetLogoPlot() {
        return netLogoPlot;
    }
}