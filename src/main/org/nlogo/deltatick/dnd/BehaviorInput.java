package org.nlogo.deltatick.dnd;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/1/12
 * Time: 9:31 PM
 * To change this template use File | Settings | File Templates.
 */
// I want this to be added to the label, but it will take data from what students enter, not from an XML file

public class BehaviorInput extends javax.swing.JTextField {
    public BehaviorInput(Component parent) {
        super();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);
        this.setToolTipText("Who?");
        this.createToolTip().setForeground(Color.blue);

        //JToolTip tTip = this.createToolTip();


        //this.getDocument().addDocumentListener(new myDocumentListener);


    }

}
