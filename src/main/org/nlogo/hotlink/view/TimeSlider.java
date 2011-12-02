package org.nlogo.hotlink.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * TimeSlider is boss. Controls view. All listen.
 * 
 * @author Michelle HWJ
 */

public class TimeSlider extends JSlider
                        implements ActionListener {

    public AdjustableTimer playTimer = new AdjustableTimer( 1 , this );
    ViewPanel viewPanel;

    public TimeSlider( ViewPanel viewPanel ) {
        super();

        this.viewPanel = viewPanel;

        setSize( 230 , getHeight() );
		setPreferredSize( new java.awt.Dimension( 230, 20 ) );
        setMinimum(0);
		setValue(0);
    }

    public void actionPerformed(ActionEvent e) {
        playTimer.setDelay( 500 - viewPanel.playerPanel.speedValues.getNumber().intValue() * 50 );
        if( getValue() < getMaximum() ) {
            setValue( getValue() + 1 );
        } else {
            playTimer.stop();
            viewPanel.playerPanel.play.toggle();
        }
    }

    public class AdjustableTimer extends Timer {
        AdjustableTimer( int delay , ActionListener listener ) {
            super( delay , listener );
            stop();
        }

        public void toggle() {
            if( this.isRunning() ) {
                stop();
            } else {
                setDelay( 500 - viewPanel.playerPanel.speedValues.getNumber().intValue() * 50 );
                start();
            }
            viewPanel.playerPanel.play.toggle();
        }
    }

    
}