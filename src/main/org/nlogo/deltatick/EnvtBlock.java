package org.nlogo.deltatick;


// Import statements copy-pasted from BreedBlock and PlotBlock -a.
//import edu.umd.cs.findbugs.gui2.OriginalGUI2ProjectFile;

import org.jfree.layout.CenterLayout;
import org.nlogo.api.NetLogoListener;
import org.nlogo.api.Shape;
import org.nlogo.app.App;
// maybe something about patch color
import org.nlogo.deltatick.xml.Breed;
// xml file of patch
import org.nlogo.deltatick.dialogs.ShapeSelector;
import org.nlogo.deltatick.xml.Envt;
import org.nlogo.deltatick.xml.OwnVar;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.hotlink.dialogs.StackedShapeIcon;
import org.nlogo.shape.VectorShape;
import org.nlogo.window.Widget;

import org.nlogo.deltatick.dnd.RemoveButton;
import org.nlogo.window.Widget;
import org.nlogo.deltatick.dnd.PrettyInput;


import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.io.IOException;
import java.util.List;

import org.nlogo.agent.Patch;

import java.awt.datatransfer.DataFlavor;

import org.nlogo.deltatick.dialogs.EnvtTypeSelector;


/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 8/24/11
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */

// EnvtBlock is for the holder block on the interface. Will have to create separate patchblocks
// to fill in the EnvtBlock -A. (aug 26)

public strictfp class EnvtBlock
        extends CodeBlock
        implements MouseMotionListener,
        MouseListener {

    JTextField envtField;
    transient Envt envt;

    public EnvtBlock(Envt e) {
        super(e.nameEnvt(), ColorSchemer.getColor(3));
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setLocation(0, 0);
        this.envt = e;
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                //CodeBlock.codeBlockFlavor,
                patchBlockFlavor,
                envtBlockFlavor,
                conditionBlockFlavor,
                CodeBlock.codeBlockFlavor
        };
        //this.setVisible(true);
        label.add(envtField);
        envtField.setText(envt.nameEnvt());
    }

    /* envt as patches
        public  String OwnVars() {
            String code = "";
            if (envt.getOwnVars().size() > 0) {
           code += "patches-own [ ";
           for (OwnVar var : envt.getOwnVars()) {
           code += var.name + " ";
       }
                code += "]\n";
            }
            return code;
        }


        public String setup() {
       String code = " ";
       if (envt.needsSetUpBlock()) {
           code += "ask patches [ " + envt.getSetupCommands() + " \n";
       }
            if (envt.needsSetUpBlock()) {
                    for ( OwnVar var : envt.getOwnVars() ) {
                        if (var.setupReporter != null ) {
                    code += "set " + var.name + " " + var.setupReporter + " \n";
                }
            }
                code += "]\n";
            }

       return code;
   }

        public String update() {
        String code = "";
        if( envt.needsUpdateBlock() ) {
            code += "ask patches [\n";
            if( envt.getUpdateCommands() != null ) { code += envt.getUpdateCommands(); }
            for( OwnVar var : envt.getOwnVars() ) {
                if( var.updateReporter != null ) {
                    code += "set " + var.name + " " + var.updateReporter + "\n";
                }
            }
            code += "]\n";
        }

        return code;
    }

    public String unPackAsCode() {
        String passBack = "";

        passBack += "ask patches" + " [\n";
        for( CodeBlock block : myBlocks ) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }
    */

    //I want to avoid declaring envts as breeds (March 24)
    /*
    public String declareEnvtBreed() {
        return "breed [ " + envt.nameEnvt() + " one-of-" + envt.nameEnvt() + " ]\n";
    }
    */



    public String OwnVars() {
        String code = "";
        if (envt.getOwnVars().size() > 0) {
            code += "patches-own [ ";
            for (OwnVar var : envt.getOwnVars()) {
                code += var.name + " ";
            }
            code += "]\n";
        }
        return code;
    }

    public String setup() {
        String code = " ";
        if (envt.needsSetUpBlock()) {
            code += "ask patches [ " + envt.getSetupCommands() + " \n";
        }
        if (envt.needsSetUpBlock()) {
            for (OwnVar var : envt.getOwnVars()) {
                if (var.setupReporter != null) {
                    code += "set " + var.name + " " + var.setupReporter + " \n";
                }
            }
            code += "]\n";
        }
        return code;
    }

    //TODO: Give patches a name such that breeds can talk directly to them
    // if pcolor = green, ask patches [ set global water ]
    public String update() {
        String code = "";
        if (envt.needsUpdateBlock()) {
            code += "ask patches [\n";
            if (envt.getUpdateCommands() != null) {
                code += envt.getUpdateCommands();
            }
            for (OwnVar var : envt.getOwnVars()) {
                if (var.updateReporter != null) {
                    code += "set " + var.name + " " + var.updateReporter + "\n";
                }
            }
            code += "]\n";
        }
        return code;
    }

    public String unPackAsCode() {
        String passBack = "";

        passBack += "ask patches [\n";
        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }

    public void makeLabel() {
        label.add(removeButton);
        label.add(new JLabel("Ask"));
        envtField = new PrettyInput(this);
        label.setBackground(getBackground());
    }


    public void mouseEnter(MouseEvent evt) {
    }

    public void mouseExit(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseMoved(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }

    int beforeDragX;
    int beforeDragY;

    int beforeDragXLoc;
    int beforeDragYLoc;

    public void mousePressed(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }

    public void mouseDragged(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc,
                point.y - beforeDragY + beforeDragYLoc);
    }

    public String envtName() {
        return envt.nameEnvt();
    }

}

