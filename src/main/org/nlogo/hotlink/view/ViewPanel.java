package org.nlogo.hotlink.view;

import org.nlogo.hotlink.graph.PlotPanel;
import org.nlogo.app.PlotTab;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ViewPanel extends javax.swing.JPanel {                                                                                                               

	View view = new View( this );
	PlayerPanel playerPanel = new PlayerPanel( this );
	TimeSlider timeSlider = new TimeSlider( this );
    PlotTab plotTab;
	
	public ViewPanel( PlotTab plotTab ) {
        setLayout( new BoxLayout( this , BoxLayout.PAGE_AXIS ) );
        //setLayout( new FlowLayout( FlowLayout.CENTER ) );
        setSize( 230 , 500 );
        this.plotTab = plotTab;
		this.add(view);
		this.add(timeSlider);
		this.add(playerPanel);
	}

    public void refresh() {
        if( getImageCount() > 0 ) {
            timeSlider.setMaximum( getImageCount() - 1 );
        }
        goToFrame(getCurrentImageCount() - 1);
            //goToFirstFrame();
            //}
    }

    public int getImageCount() {
        return view.getImageCount();
    }

    public int getCurrentImageCount() {
        return view.getCurrentImageCount();
    }
	
	public void renew() {
		view.renew();
		this.setVisible(true);
	}

    // when the graphpanel is passed in, go ahead and tack the
    // view on to listen too.
    public void addChangeListener( PlotPanel graphPanel ) {
        timeSlider.addChangeListener( graphPanel );
        timeSlider.addChangeListener( view );
    }

    // Navigation Utilities
    public void goToNextFrame() {
        timeSlider.setValue( timeSlider.getValue() + 1 );
    }

    public void goToPrevFrame() {
        timeSlider.setValue( timeSlider.getValue() - 1 );
    }

    public void goToFirstFrame() {
        timeSlider.setValue(0);
    }

    public void goToLastFrame() {
        timeSlider.setValue( getCurrentImageCount() - 1 );
        view.showImage(getCurrentImageCount() - 1);
    }

    public void goToFrame( int myInt ) {
        timeSlider.setValue( myInt );
    }

    public View getView() {
        return view;
    }

    public PlotTab getPlotTab() {
        return plotTab;
    }

    public TimeSlider getTimeSlider() {
        return timeSlider;
    }

    public Insets getInsets() {
        return new Insets( 4, 4, 5, 5);
    }
}