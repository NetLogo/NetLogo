package org.nlogo.hotlink.graph;

import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.*;

public class LinePlotPenPanel extends PenPanel {

    // Variables declaration - do not modify
    private javax.swing.JLabel deltaLabel;
    private javax.swing.JLabel deltaValue;
    private JLabel wholeLabel;
    Color color;
    // End of variables declaration

    public LinePlotPenPanel( String penName , Color color ) {
        super( penName , color );
        this.color = color;
        initComponents( penName, color );
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents( String penName , Color color ) {
        deltaLabel = new javax.swing.JLabel();
        deltaValue = new javax.swing.JLabel();

        wholeLabel = new javax.swing.JLabel();

        wholeLabel.setForeground(color);
        setDeltaLabel(penName);

        setLayout( new BoxLayout( this , BoxLayout.Y_AXIS ) );
        add(checkBox);
        add(wholeLabel);
    }

    public void setDeltaValue( double value ) {
        DecimalFormat df = new DecimalFormat("#.0#");
        if( value < 0 ) {
            deltaValue.setText( "(" + df.format( value ) + ")" );
        } else {
            deltaValue.setText( "(+" + df.format( value ) + ")" );
        }
        wholeLabel.setText(deltaLabel.getText() + deltaValue.getText());
    }

    public void setDeltaLabel( String label ) {
        wholeLabel.setText(deltaLabel.getText() + deltaValue.getText());
    }

    public Color getColor() { return color; };
}