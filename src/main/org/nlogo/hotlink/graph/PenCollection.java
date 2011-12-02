package org.nlogo.hotlink.graph;

import javax.swing.*;
import java.awt.*;

public class PenCollection extends javax.swing.JPanel {
    java.util.List<PenPanel> pens = new java.util.ArrayList<PenPanel>();
	
	PenCollection() {
        BoxLayout layout = new BoxLayout( this, BoxLayout.PAGE_AXIS );
		setLayout( layout );
	    //setPreferredSize( new java.awt.Dimension( 225 , 150 ) );
	    setMinimumSize( getPreferredSize() ) ;
	    setMaximumSize( getPreferredSize() ) ;
	}
	
	public void addPen( String penName , PlotWindow plot, Color color ) {
        PenPanel pen = null;

        if( plot.getClass() == LinePlot.class ) {
    		pen = new LinePlotPenPanel( penName , color );
        } else {
            pen = new HistoPlotPenPanel( penName , color );
        }

        pen.setAlignmentX( Component.LEFT_ALIGNMENT );
		add( pen );
		pens.add(pen);
		pen.addChangeListener( plot );
	}

    public PenPanel getPen( String name ) {
        //System.out.println("getPen " + name);
        for( PenPanel penPanel : pens ) {
            //System.out.println(penPanel.getName());
            //System.out.println(penPanel.getCheckBox().getName());
            if( penPanel.getName() == name ) {
                return penPanel;
            }
        }
        return null;
    }
	
	public boolean isPen( int index ) {
		if( pens.get( index ) != null ) {
			return true;
		}
		return false;
	}
	
	public PenPanel getPen( int index ) {
		return pens.get( index );
	}

}