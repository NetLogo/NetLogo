package org.nlogo.hotlink.view;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JButton;

class MediaButton extends JButton {

    MediaButton( PlayerPanel playerPanel ) {
        super();
        setup( playerPanel );
    }

    MediaButton( String str , PlayerPanel playerPanel  ) {
        super( str );
        setup( playerPanel );
    }

    MediaButton( ImageIcon icon , PlayerPanel playerPanel  ) {
        super( icon );
        setup( playerPanel );
    }

    private void setup( PlayerPanel playerPanel ) {
        this.setSize( new Dimension(38,38) );
        this.setPreferredSize( new Dimension(38,38) );
        this.addActionListener( playerPanel );
        playerPanel.add(this);
    }
}