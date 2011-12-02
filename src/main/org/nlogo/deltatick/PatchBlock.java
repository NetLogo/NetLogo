package org.nlogo.deltatick;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 8/26/11
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;



import org.nlogo.window.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.HashMap;



public strictfp class PatchBlock
	extends CodeBlock {

	public PatchBlock(String name)
	{
        super( name , ColorSchemer.getColor(4) );
        flavors = new DataFlavor[] {
          DataFlavor.stringFlavor,
          CodeBlock.codeBlockFlavor
        };
	}

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
    // extracting name of behavior into "to go" -A. (sept 24)
    public String unPackAsCommand() {
        String passBack = "";
        passBack += " " + getName() + " ";
        //passBack += "ask patches [";
        for( JTextField input : inputs.values() ) {
            passBack += input.getText() + " ";
            //System.out.println("I'm getting here");
        }
        passBack += "\n";
        //passBack += "]\n" + "end";

        return passBack;
    }

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

    public void addBlock( CodeBlock block ) {
        super.addBlock( block );
        //this.validate();
    }

    public void makeLabel() {
        JLabel name = new JLabel( getName() );
        java.awt.Font font = name.getFont();
        name.setFont( new java.awt.Font( "Arial" , font.getStyle() , 12 ) );
        label.add(removeButton);
        removeButton.setVisible(false);
        label.setBackground(getBackground());
        label.add( name );
    }
}
