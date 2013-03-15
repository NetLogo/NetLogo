package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.TraitBlock;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 5/15/12
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariationDropDown extends JComboBox {
    ArrayList<String> variationList;
    TraitBlock myParent;
    String [] variationArray;
    String selectedVariation;

    public VariationDropDown (ArrayList<String> variationList, TraitBlock myParent) {
        this.variationList = new ArrayList<String>(variationList);
        //variationArray = new String[variationList.size()];
        variationArray = variationList.toArray(new String[variationList.size()]);
        //variationArray = variationList.toArray(variationArray);
        this.setModel(new javax.swing.DefaultComboBoxModel(variationArray));
        this.myParent = myParent;
        // TODO figure out a way that this doesn't break when transferred
        //this.setAction(updateTraitLabel);

    }

    private final javax.swing.Action updateTraitLabel =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                  // myParent.newLabel();
                    //myParent.updateNumber();
                }
};

    public String getSelectedVariation () {
        selectedVariation = this.getSelectedItem().toString();
        return selectedVariation;

    }
}
