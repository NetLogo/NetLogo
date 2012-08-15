package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.TraitBlock;

import java.awt.*;
import java.util.HashMap;
import org.nlogo.deltatick.TraitBlock;
import org.nlogo.deltatick.xml.Variation;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/21/12
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnergyInput extends javax.swing.JTextField {

    public EnergyInput(Component parent) {
        super();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        setBackground( Color.white );
        setFont(new java.awt.Font("Courier New", 1, 12));
        setSize(this.getWidth(), 10);
        this.setToolTipText("How fast?");
    }


}
