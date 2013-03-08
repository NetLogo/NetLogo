package org.nlogo.deltatick;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/7/13
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class LabelPanel extends JPanel {
    JLabel label;
    JCheckBox checkBox;
    HashMap<JCheckBox, String> checkBoxSelectedHashMap = new HashMap<JCheckBox, String>();
    ArrayList<String> traitNames = new ArrayList<String>();
    HashMap<String, JCheckBox> allCheckBoxes = new HashMap<String, JCheckBox>();


    public LabelPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.traitNames.add("age");
        this.traitNames.add("energy");
        initiComponents(traitNames);
    }

    public void initiComponents(ArrayList<String> list) {
        for (String nam : list) {
            JCheckBox box = new JCheckBox(nam);
            allCheckBoxes.put(nam, box);
            this.add(box);
        }
    }

    public void addTraitCheckBox(String trait) {
        JCheckBox box = new JCheckBox(trait);
        this.add(box);
    }

    public void updateData(HashMap<String, TraitState> t) {
        if (t.size() != traitNames.size()) {
            for (JCheckBox box : allCheckBoxes.values()) {    //remove visually
            this.remove(box);
        }
            allCheckBoxes.clear();
            traitNames.clear();
            traitNames.add("age");
            traitNames.add("energy");
            for (String string : t.keySet()) {
                traitNames.add(string);
            }
        initiComponents(traitNames);

        }
    }

    public HashMap<String, JCheckBox> getCheckBoxes() {
        return allCheckBoxes;
    }


//    public void updateCheckBox(String traitName, HashMap<String, String> varPercent) {
//        // Check if trait has been added
//        if (traitNames.contains(traitName) == false) {
//
//            // Trait not previously present
//            // Create corresponding charts
//            JCheckBox box = new JCheckBox(traitName);
//            traitNames.add(traitName);
//
//
//            this.validate();
//
//        }
//
//        // Check is trait is to be removed
//        if (varPercent.size() == 0) {
//            // Remove corresponding panel
//            this.remove(chartsPanelMap.get(traitName));
//            this.validate();
//
//            chartsPanelMap.remove(traitName);
//        }
//
//    }

//    public void updateCheckBoxes (){
//        JCheckBox box = new JCheckBox()
//    }
}
