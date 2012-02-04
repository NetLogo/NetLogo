package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Breed;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/4/12
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */

// this block will hold behaviors for agents with this variation

    // not sure if this should be abstract -Feb 4
public strictfp class VariationBlock
    extends CodeBlock
    //implements MouseMotionListener,
    //MouseListener
    {
        JTextField name;
        //transient Variation variation;          // doesn't make sense here because it's not coming from XML file
        String variation;
        String breedName;

        // Change the first argument to breed later
        public VariationBlock ( String breed, String v ) {
            super ("nameVariation", ColorSchemer.getColor(3));     // change color & name
            this.variation = v;
            this.breedName = breed;


        }


}
