package org.nlogo.deltatick;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 5/7/12
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */

public class DistanceInput extends javax.swing.JTextField {

    public DistanceInput(Component parent) {
        super();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        //setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);
        this.setToolTipText("Within how much distance?");
    }

}
