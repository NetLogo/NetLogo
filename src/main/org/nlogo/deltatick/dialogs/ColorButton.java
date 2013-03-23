package org.nlogo.deltatick.dialogs;

import org.nlogo.deltatick.BreedBlock;
import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.QuantityBlock;
import org.nlogo.deltatick.TraitBlock;
import org.nlogo.window.ColorDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 5/13/12
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorButton extends JButton {
    TraitBlock myParentTrait;
    QuantityBlock myParentQuantity;
    BreedBlock myParent;
    transient Frame myFrame;
    String selectedColor;
    ColorDialog colorDialog;
    Color color;
    Boolean checkForColor;

    public ColorButton (Frame myFrame, TraitBlock myParent) {
        this.setAction(pickColorAction);
        this.myParentTrait = myParent;

        //setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        setPreferredSize(new Dimension(16, 16));
        try {
            Image img = ImageIO.read(getClass().getResource("/images/deltatick/brush_16.png"));
            setIcon(new ImageIcon(img));
        }
        catch (IOException ex) {
        }
        //setForeground(java.awt.Color.gray);
        setBorderPainted(true);
        setMargin(new java.awt.Insets(1, 1, 1, 1));
        checkForColor = false;

    }

    public ColorButton (Frame myFrame, BreedBlock myParent) {
        System.out.println("Constr 2");
        this.myFrame = myFrame;
        this.myParent = myParent;
        this.setAction(pickColorActionBreed);
        setText("Pick color");
        checkForColor = false;
    }

    //constructor being used
    public ColorButton (Frame myFrame, QuantityBlock myParent) {
        this.setAction(pickColorActionQuantity);
        this.myParentQuantity = myParent;

        //setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        //setBorderPainted(true);
        setPreferredSize(new Dimension(16, 16));
        try {
            Image img = ImageIO.read(getClass().getResource("/images/deltatick/brush_16.png"));
            setIcon(new ImageIcon(img));
        }
        catch (IOException ex) {
        }
        setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkForColor = false;

    }

    //action for color button on breedblock - Aditi (Feb 22, 2013)
    private final Action pickColorActionBreed =
            new AbstractAction("Pick color") {
                public void actionPerformed(ActionEvent e) {
                    colorDialog = new ColorDialog(myFrame, true);
                    colorDialog.showDialog();
                    color = colorDialog.getSelectedColor();
                    myParent.setColorName(colorDialog.getSelectedColorString());
                    changeColor();

                    checkForColor = true;
                }
            };


    // action for color button on traitBlock -Aditi (Jan 17, 2013)
    private final javax.swing.Action pickColorAction =
            new javax.swing.AbstractAction("Pick color") {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    colorDialog = new ColorDialog(myFrame, true);
                    colorDialog.showDialog();
                    color = colorDialog.getSelectedColor();
                    myParentTrait.addVarColor();
                    //myParent.addVarColorName();
                    checkForColor = true;
                    myParentTrait.setButtonColor(color);
                    //changeButtonColor(color);
                }
            };

    //action for color button on Quantity Block - Aditi (Jan 17, 2013)
    private final javax.swing.Action pickColorActionQuantity =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    colorDialog = new ColorDialog(myFrame, true);
                    colorDialog.showDialog();
                    color = colorDialog.getSelectedColor();
                    //myParentQuantity.addVarColor();
                    checkForColor = true;
                    myParentQuantity.setButtonColor(color);
                }
            };

    public String getSelectedColorName() {
        return colorDialog.getSelectedColorString();
    }

    public Color getSelectedColor() {
        return colorDialog.getSelectedColor();
    }

    /*
    public int getSelectedColorValue() {
        return colorDialog.getSelectedColor().
    }
    */

    public boolean gotColor() {
        return checkForColor;

    }

    public void changeColor() {
        setOpaque(true);
        setBackground(colorDialog.getSelectedColor());

    }




}
