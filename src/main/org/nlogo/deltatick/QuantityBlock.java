package org.nlogo.deltatick;

import org.nlogo.window.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
//import org.nlogo.deltatick.xml.Breed;

public strictfp class QuantityBlock
        extends CodeBlock {

    transient JPanel penColorButton;
    Color penColor = Color.black;
    boolean histo = false;
    String bars = "0";
    String trait = " ";

    public QuantityBlock(String name, boolean histo, String bars, String trait) {
        super(name, ColorSchemer.getColor(2));
        this.histo = histo;
        this.bars = bars;
        this.trait = trait;
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                quantityBlockFlavor
        };
        label.add(makeBreedShapeButton());
        // - quantity block need not have shape change -A. (sept 30)
    }

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }


    public String unPackAsProcedure() {
        System.out.println("unPackAsProcedure");
        String passBack = "";
        passBack += "to-report " + getName();
        if (inputs.size() > 0) {
            passBack += " [ ";
            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
            passBack += "]";
        }
        passBack += "\n";
        passBack += "report " + code;
        passBack += "\n";
        passBack += "end";
        passBack += "\n";
        passBack += "\n";

        return passBack;
    }


    public String unPackAsCommand() {
        String passBack = "";

        if (histo) {
            passBack += "set-histogram-num-bars " + bars + "\n";
            //passBack += "set-plot-x-range 0 max " + getName() + " ";
            //passBack += "plotxy" + x + y + "\n";
            for (JTextField input : inputs.values()) {
                passBack += input.getText() + " ";
            }
            passBack += "\n";
            //passBack += "histogram " + getName() + " \n";
            passBack += "histogram [ " + trait + " ] of turtles";
        } else {
            passBack += "plot " + getName() + " ";
        }
        for (JTextField input : inputs.values()) {
            passBack += input.getText() + " ";
        }
        passBack += "\n";

        return passBack;
    }

    public Map<String, JTextField> getInputs() {
        return inputs;
    }

    public JPanel makeBreedShapeButton() {
        return new BreedShapeButton();
    }

    class BreedShapeButton extends JPanel implements MouseListener {

        BreedShapeButton() {
            this.setSize(30, 30);
            this.setBackground(penColor);
            this.addMouseListener(this);
            //this.setComponentZOrder(this.getParent(),0);
            this.repaint();
        }

        public void mouseReleased(java.awt.event.MouseEvent event) {
        }

        public void mouseEntered(java.awt.event.MouseEvent event) {
        }

        public void mouseExited(java.awt.event.MouseEvent event) {
        }

        public void mousePressed(java.awt.event.MouseEvent event) {
        }

        public void mouseClicked(java.awt.event.MouseEvent event) {
            penColor = JColorChooser.showDialog(null, "Pick a pen color...", java.awt.Color.BLACK);
            //if( penColor == null ) { penColor = java.awt.Color.BLACK; }
            this.setBackground(penColor);
            this.setForeground(penColor);
            this.setVisible(true);
            //System.out.println(penColor);
            //System.out.println(penColor.getRGB());
        }

    }

    private final javax.swing.Action colorAction =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JColorChooser.showDialog(null, "Pick a pen color...", java.awt.Color.BLACK);

                    //System.out.println(penColor);
                    //System.out.println(penColor.getRGB());
                    //penColorButton.setBackground( penColor );
                }
            };

    public Color getPenColor() {
        //System.out.println(penColor);
        return penColor;
    }
}