package org.nlogo.hotlink.view;

import javax.swing.ImageIcon;

class PlayButton extends MediaButton {

    ImageIcon playImage = new ImageIcon(getClass().getResource("/images/hotlink/Pause.jpg"));
    ImageIcon pauseImage = new ImageIcon(getClass().getResource("/images/hotlink/Play.jpg"));

    PlayButton( PlayerPanel playerPanel ) {
        super( playerPanel );
        setIcon( pauseImage );
    }

    void toggle() {
        if( getIcon() == playImage ) {
            setIcon( pauseImage );
        } else {
            setIcon( playImage );
        }
    }
}