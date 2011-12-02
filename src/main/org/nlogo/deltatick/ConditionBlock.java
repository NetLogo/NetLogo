package org.nlogo.deltatick;

import org.nlogo.window.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.HashMap;

public strictfp class ConditionBlock
	extends CodeBlock {

	public ConditionBlock( String name )
	{
        super( name , ColorSchemer.getColor(1) );
        flavors = new DataFlavor[] {
          //DataFlavor.stringFlavor,
          conditionBlockFlavor,
          CodeBlock.codeBlockFlavor,
          CodeBlock.patchBlockFlavor,
                CodeBlock.envtBlockFlavor
        };
	}
    //codeBlockFlavor is what makes Condition Blocks valid for Breed Block -a. (Sept 6)

    public void setMyParent( CodeBlock block ) {
        myParent = block;
    }

    // dragged from the library, or used to write full procedures.
    public String unPackAsCode() {
        if( myParent != null ) {
            return unPackAsCommand();
        }
        return unPackAsProcedure();
    }

    // dragged from a breed block, or used to write the full procedures.
    public String unPackAsCommand() {
        String passBack ="";

        passBack += "if " + getName() + " ";
        for( JTextField input : inputs.values() ) {
            passBack += input.getText() + " ";
        }
        passBack += "[\n";
        for( CodeBlock block : myBlocks ) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    };

    public String unPackAsProcedure() {
        String passBack = "";

        passBack += "to-report " + getName() + " ";

        if( inputs.size() > 0 ) {
            passBack += "[ ";
            for( String input : inputs.keySet() ) {
                passBack += input + " ";
            }
            passBack += "]";
        }
        passBack += "\n";

        passBack += "  if " + code + "\n";
        passBack += "    [ report true ]\n";
        passBack += "  report false\n";

        passBack += "end\n\n";

        return passBack;
    }

    public void addBlock( CodeBlock block ) {
        super.addBlock( block );
        //this.validate();
    }

    // this code is the label you see on the condition block
    public void makeLabel() {
        JLabel name = new JLabel( "if " + getName());
        java.awt.Font font = name.getFont();
        name.setFont( new java.awt.Font( "Arial" , font.getStyle() , 12 ) );
        label.add(removeButton);
        removeButton.setVisible(false);
        label.setBackground(getBackground());
        label.add( name );
    }
}