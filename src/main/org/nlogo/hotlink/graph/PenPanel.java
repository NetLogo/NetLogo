package org.nlogo.hotlink.graph;

import java.awt.*;
import javax.swing.JCheckBox;

public abstract class PenPanel extends javax.swing.JPanel {

    JCheckBox checkBox = new JCheckBox();

    PenPanel( String penName , Color color ) {
        super();
        setName(penName);
        setLayout( new FlowLayout( FlowLayout.LEFT ));
        if( penName.length() > 10 ) {
            checkBox.setText( penName.substring(0,8) + "..." );
        } else {
            checkBox.setText( penName );
        }
        checkBox.setForeground( color );
        //checkBox.setName( index.toString() );
        checkBox.setSelected( true );
        checkBox.setEnabled(false);

        add(checkBox);
    }

    public JCheckBox getCheckBox() {
        return this.checkBox;
    }
    
    public void addChangeListener( PlotWindow plot ) {
        checkBox.addChangeListener( plot );
    }                                                            

    public void setDeltaValue( double value ) { }
    public Color getColor() { return new Color(0,0,0); }


}