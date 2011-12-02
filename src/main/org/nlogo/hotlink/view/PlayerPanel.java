package org.nlogo.hotlink.view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * PlayerPanel includes control buttons for the ViewPanel
 *
 * @author      Michelle HWJ
 * @version     %I%, %G%
 * @since       1.0
 */
public class PlayerPanel extends JPanel
							implements ActionListener {
	ViewPanel viewPanel;

	PlayButton play;
	MediaButton step;
	MediaButton ff;
	MediaButton rw;
	MediaButton start;
	MediaButton end;

    SpinnerNumberModel speedValues = new SpinnerNumberModel( 8 , 1 , 10 , 1 );
    JSpinner speed = new JSpinner( speedValues );

	public PlayerPanel( ViewPanel viewPanel ) {
		super();
		this.viewPanel = viewPanel;
		
		this.setLayout( new FlowLayout() );
		
		start = new MediaButton( new ImageIcon(getClass().getResource("/images/hotlink/StepBackward.jpg")) , this );
		play = new PlayButton( this );
		end = new MediaButton( new ImageIcon(getClass().getResource("/images/hotlink/StepForward.jpg")) , this );

        add(speed);
	}
	

	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == start ) {
			viewPanel.goToFirstFrame();
		}
		if( e.getSource() == play ) {
            viewPanel.timeSlider.playTimer.toggle();
		}
		if( e.getSource() == step ) {
			viewPanel.goToNextFrame();
		}
		if( e.getSource() == end ) {
			viewPanel.goToLastFrame();
		}
	}

    public int getSpeed() {
        return speedValues.getNumber().intValue();
    }
}
