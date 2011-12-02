package org.nlogo.deltatick ;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;

import org.nlogo.window.Widget;

import javax.swing.*;

// strictfp: When applied to a class, all calculations inside the class use strict floating-point math.-a.

public strictfp class BehaviorBlock
	extends CodeBlock {

	public BehaviorBlock( String name )
	{
        // CodeBlock(String name, Color color) - calling parent block -a.
		super( name , ColorSchemer.getColor(0).brighter() );
        flavors = new DataFlavor[] {
                //DataFlavor.stringFlavor,
                CodeBlock.behaviorBlockFlavor,
                CodeBlock.codeBlockFlavor,
        };
	}
    //codeBlockFlavor in Condition Block, Beh Block is what makes it a valid block for Breed   -A.

    public String unPackAsCode() {
        if( myParent == null ) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }

    //extracting the code under procedure -A. (Sept 24)
    public String unPackAsProcedure() {
        String passBack = "";
        passBack += "to " + getName() + " ";

        if( inputs.size() > 0 ) {
            passBack += "[ ";
            for( String input : inputs.keySet() ) {
                passBack += input + " ";
            }
            passBack += "]";
        }

        // 'code' is a string variable declared in CodeBlock
        passBack += "\n" + code + "\n";
        passBack += "end\n\n";

        return passBack;
    }

    // extracting name of behavior into "to go" -A. (sept 24)
    public String unPackAsCommand() {
        String passBack = "";
        
        passBack += " " + getName() + " ";
        for( JTextField input : inputs.values() ) {
            passBack += input.getText() + " ";
        }
        passBack += "\n";

        return passBack;
    }
}
