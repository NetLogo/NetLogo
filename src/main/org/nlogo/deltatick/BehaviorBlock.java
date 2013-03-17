package org.nlogo.deltatick;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;
import java.util.Map;

import org.nlogo.deltatick.dnd.BehaviorInput;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;

// strictfp: When applied to a class, all calculations inside the class use strict floating-point math.-a.

public strictfp class BehaviorBlock
        extends CodeBlock {

    public BehaviorBlock(String name) {
        super(name, ColorSchemer.getColor(0).brighter());
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.behaviorBlockFlavor,
                CodeBlock.codeBlockFlavor,
        };
    }
    //codeBlockFlavor in Condition Block, Beh Block is what makes it a valid block for Breed   -A.

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }
      //TODO: Box bracket appears when it need not (March 9)
    //extracting the argument to be passed after name of procedure (March 2)
    public String unPackAsProcedure() {
        String passBack = "";
        passBack += "to " + getName() + " ";

        if ((agentInputs.size() > 0) || (inputs.size() > 0) || (behaviorInputs.size() > 0) ) {
            passBack += "[ ";
        }
        if (inputs.size() > 0) {
            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
        }
        if (agentInputs.size() > 0) {
            for (String s : agentInputs.keySet()) {
                passBack += s + " ";
            }
        }
        if (behaviorInputs.size() > 0) {
            for (String s : behaviorInputs.keySet()) {
                passBack += s + " ";
            }
        }
        if ((agentInputs.size()  > 0) || (inputs.size() > 0) || (behaviorInputs.size() > 0) ) {
            passBack += " ]";
        }
        if ( ifCode != null ) {
        passBack += "\n" + ifCode + "[\n" + code + "\n" + "]";
        }
        //TODO commented out on March 9, testing

        else {
            passBack += "\n" + code + "\n";
        }

        if (energyInputs.size() > 0) {
            for (JTextField inputName : energyInputs.values()) {
                passBack += "set energy energy " + inputName.getText() + "\n";
            }
        }
        passBack += " end\n\n";

        return passBack;
    }

    // extracting name of behavior into "to go" -A. (sept 24)
    public String unPackAsCommand() {
        String passBack = "";

        passBack += " " + getName() + " ";
        for (JTextField input : inputs.values()) {
            passBack += input.getText() + " ";
        }
        for (JTextField agentInput : agentInputs.values()) {
            passBack += agentInput.getText() + " ";
        }
        for (JTextField behaviorInput :behaviorInputs.values()) {
            passBack += behaviorInput.getText() + " ";
        }

        passBack += "\n";
        //Commented out because I don't want a fixed value to be set for a variable that's not a trait -Aditi (Feb 22, 2013)
//        if (myParent instanceof BreedBlock) {
//            for (String name : ((BreedBlock) myParent).myUsedBehaviorInputs) {
//                for (String s : behaviorInputs.keySet()) {
//                    if (name.equals(s)) {
//                        passBack += "set " + name + " " + behaviorInputs.get(name).getText().toString() + "\n";
//                    }
//                }
//                }
//            }
        return passBack;
    }

    public void updateBehaviorInput() {
        Container parent = getParent();
        if (parent instanceof TraitBlock) {
            HashMap<String, Variation> hashMap = ((TraitBlock) parent).variationHashMap;
            String selectedVariationName = ((TraitBlock) parent).getDropdownList().getSelectedItem().toString();
            String trait = ((TraitBlock) parent).getName();
            Variation tmp = hashMap.get(selectedVariationName);

            String value = tmp.value;
            for ( String s : behaviorInputs.keySet()) {
                if (s.equals(trait)) {
                    JTextField textField = behaviorInputs.get(s);
                    textField.setText(value);
                }
            }
        }
    }

    // will work only for one behaviorInput per block -A. (Aug 10, 2012)
    public String getBehaviorInputName() {
        String behaviorInputName = new String();
        for ( String s : behaviorInputs.keySet()) {
            behaviorInputName = s;
            }
        return behaviorInputName;
    }

    public String getAgentInputName() {
        String agentInputName = new String();
        for ( String s : agentInputs.keySet()) {
            agentInputName = s;
            }
        return agentInputName;
    }
}
