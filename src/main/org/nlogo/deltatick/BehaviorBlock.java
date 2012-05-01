package org.nlogo.deltatick;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;

import org.nlogo.deltatick.dnd.AgentInput;
import org.nlogo.window.Widget;

import javax.swing.*;

// strictfp: When applied to a class, all calculations inside the class use strict floating-point math.-a.

public strictfp class BehaviorBlock
        extends CodeBlock {

    transient AgentInput agentInput;
    String agentName;



    public BehaviorBlock(String name) {
        // CodeBlock(String name, Color color) - calling parent block -a.
        super(name, ColorSchemer.getColor(0).brighter());
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.behaviorBlockFlavor,
                CodeBlock.codeBlockFlavor,
        };
        agentInput = new AgentInput( this );
        agentName = agentInput.getText();

        /*
        agentInput = new AgentInput(this);
        agentInput.setText(getName());
        label.add(agentInput);
        */
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
        //passBack += "to " + getName() + " " + agentInput();
        passBack += "to " + getName() + " ";

        if (inputs.size() > 0) {
            passBack += "[ ";
            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
            if (agentInputs.size() > 0) {
            //passBack += "[ ";
            for (String inputName : agentInputs.keySet()) {
                passBack += inputName + " ";
            }

        }

        }

        else if (agentInputs.size() > 0) {
            passBack += "[ ";
            for (String inputName : agentInputs.keySet()) {
                passBack += inputName + " ";
            }

        }

        if (inputs.size() > 0 || agentInputs.size() > 0) {
            passBack += "]";
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
        for (JTextField inputName : agentInputs.values()) {
            passBack += inputName.getText() + " ";
        }
        passBack += "\n";

        return passBack;
    }
}
