package org.nlogo.deltatick.dnd;

import com.sun.media.ui.ToolTip;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: May 13, 2010
 * Time: 11:03:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrettyInput extends javax.swing.JTextField {
    private JToolTip toolTip;

    public PrettyInput(Component parent) {
        super();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);

        // Create the tooltip
        toolTip = super.createToolTip();
        toolTip.setForeground(Color.white);
    }

    public JToolTip createToolTip() {
        toolTip = super.createToolTip();
        toolTip.setBackground(Color.white);
        toolTip.setForeground(Color.black);
        return toolTip;
    }

    public JToolTip getToolTip() {
        return toolTip;
    }
}

