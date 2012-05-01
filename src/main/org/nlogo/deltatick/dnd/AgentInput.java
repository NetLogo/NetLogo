package org.nlogo.deltatick.dnd;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/1/12
 * Time: 9:31 PM
 * To change this template use File | Settings | File Templates.
 */
// I want this to be added to the label, but it will take data from what students enter, not from an XML file

public class AgentInput extends javax.swing.JTextField {
    public AgentInput( Component parent ) {
        super();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);

    }
}
