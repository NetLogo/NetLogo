package org.nlogo.deltatick.dnd;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: May 13, 2010
 * Time: 11:03:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrettyInput extends javax.swing.JTextField {

    public PrettyInput( Component parent ) {
        super();

        setBorder( javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder( parent.getBackground().darker() ),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ) );
        //setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize( this.getWidth() , 10 );
    }

}
