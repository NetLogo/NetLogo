package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.TraitBlock;
import org.nlogo.window.ColorDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 5/13/12
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorButton extends JButton {
    TraitBlock myParent;
    transient Frame myFrame;
    String selectedColor;
    ColorDialog colorDialog;
    Color color;
    Boolean checkForColor;

    public ColorButton (Frame myFrame, TraitBlock myParent) {
        this.setAction(pickColorAction);
        this.myParent = myParent;

        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        setBorderPainted(true);
        //setMargin(new java.awt.Insets(2, 2, 2, 2));
        this.setMaximumSize(new Dimension(3, 4));
        this.setMinimumSize(new Dimension(3, 4));
        this.getPreferredSize();
        this.setText("color");
        checkForColor = false;

    }

    private final javax.swing.Action pickColorAction =
            new javax.swing.AbstractAction("C") {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    colorDialog = new ColorDialog(myFrame, true);
                    colorDialog.showDialog();
                    color = colorDialog.getSelectedColor();
                    myParent.addVarColor();
                    //myParent.addVarColorName();
                    checkForColor = true;
                    myParent.setButtonColor(color);
                    //changeButtonColor(color);
                }
            };

    public String getSelectedColorName() {
        return colorDialog.getSelectedColorString();
    }

    public Color getSelectedColor() {
        return colorDialog.getSelectedColor();
    }

    public boolean gotColor() {
        return checkForColor;

    }


    /*
    public void changeButtonColor(Color color) {
        myParent.setButtonColor(color);

    }

    public void updateColor() {
        myParent.getDropDownList().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                myParent.newLabel();
            }
        });
    }
    */

}
