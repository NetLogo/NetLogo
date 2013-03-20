package org.nlogo.deltatick.dnd;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/19/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PercentInput extends PrettyInput {
    public PercentInput(Component parent) {
        super(parent);

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);
        this.createToolTip();
        this.setToolTipText("What percent");
}
}
